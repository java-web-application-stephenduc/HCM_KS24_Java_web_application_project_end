package com.rikkei.salsp.service.student;

import com.rikkei.salsp.dto.student.BookingRequestDto;
import com.rikkei.salsp.dto.common.DepartmentDto;
import com.rikkei.salsp.dto.lecturer.LecturerSummaryDto;
import com.rikkei.salsp.dto.student.SlotDto;
import com.rikkei.salsp.entity.session.MentoringSession;
import com.rikkei.salsp.entity.session.SessionStatus;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserRole;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.exception.SlotConflictException;
import com.rikkei.salsp.repository.common.DepartmentRepository;
import com.rikkei.salsp.repository.user.LecturerRepository;
import com.rikkei.salsp.repository.session.MentoringSessionRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý quy trình đặt lịch cố vấn học tập.
 *
 * TRÁCH NHIỆM:
 * - Cung cấp danh sách giảng viên theo khoa
 * - Gợi ý khung giờ trống của giảng viên
 * - Kiểm tra xung đột lịch (conflict detection)
 * - Tạo booking mới với validations nghiêm ngặt
 *
 * KIẾN TRÚC:
 * - @Transactional(readOnly=true): Hầu hết methods chỉ đọc DB (không modify)
 * - Chỉ @Transactional (write) cho createBooking()
 *
 * BUSINESS RULES:
 * 1. Sinh viên không thể đặt lịch quá khứ
 * 2. Sinh viên không thể đặt giờ_kết_thúc <= giờ_bắt_đầu
 * 3. Giảng viên không được có 2 buổi cùng giờ trong cùng ngày
 * 4. Chỉ được đặt với User có role = LECTURER
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;
    private final MentoringSessionRepository sessionRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách tất cả khoa/ban chuyên môn.
     *
     * USE CASE:
     * - Sinh viên bước 1: Chọn khoa bước 2: Chọn giảng viên thuộc khoa đó
     *
     * FLOW:
     * 1. Query tất cả Department từ database
     * 2. Map entity sang DTO (loại bỏ sensitive data)
     * 3. Trả về list DTO
     *
     * @return List<DepartmentDto> danh sách khoa
     */
    public List<DepartmentDto> getDepartments() {
        return departmentRepository.findAll().stream()
            .map(dept -> {
                DepartmentDto dto = new DepartmentDto();
                dto.setId(dept.getId());
                dto.setName(dept.getName());
                dto.setCode(dept.getCode());
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách giảng viên áp dụng từ một khoa.
     *
     * USE CASE:
     * - Sinh viên bước 1 chọn khoa -> Bước 2 UI render danh sách giảng viên
     * - AJAX call tới endpoint /lecturer/lecturers?departmentId=5
     *
     * QUERY OPTIMIZATION:
     * - lecturerRepository.findByDepartmentIdWithProfile() dùng JOIN FETCH
     * - Tránh N+1 query khi loop lecturer -> user -> profile
     *
     * @param departmentId ID của khoa
     * @return List<LecturerSummaryDto> danh sách giảng viên từ khoa đó
     */
    public List<LecturerSummaryDto> getLecturersByDepartment(Long departmentId) {
        return lecturerRepository.findByDepartmentIdWithProfile(departmentId).stream()
            .map(lecturer -> {
                LecturerSummaryDto dto = new LecturerSummaryDto();
                dto.setId(lecturer.getUser().getId());
                dto.setFullName(lecturer.getUser().getProfile().getFullName());
                dto.setTitle(lecturer.getTitle());
                dto.setDepartmentName(lecturer.getDepartment().getName());
                dto.setDepartmentId(lecturer.getDepartment().getId());
                dto.setBio(lecturer.getBio());
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một giảng viên để hiển thị ở bước 3 (xác nhận).
     *
     * USE CASE:
     * - Sinh viên bước 3: Xác nhận chọn giảng viên + khoảng giờ
     * - Hiển thị tên, chuyên môn, ghi chú của giảng viên
     *
     * @param lecturerId ID tài khoản giảng viên (User.id)
     * @return LecturerSummaryDto thông tin giảng viên
     * @throws ResourceNotFoundException nếu không tìm thấy
     */
    public LecturerSummaryDto getLecturerSummary(Long lecturerId) {
        return lecturerRepository.findByUserId(lecturerId)
            .map(lecturer -> {
                LecturerSummaryDto dto = new LecturerSummaryDto();
                dto.setId(lecturer.getUser().getId());
                dto.setFullName(lecturer.getUser().getProfile().getFullName());
                dto.setTitle(lecturer.getTitle());
                dto.setDepartmentName(lecturer.getDepartment().getName());
                dto.setDepartmentId(lecturer.getDepartment().getId());
                dto.setBio(lecturer.getBio());
                return dto;
            })
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
    }

    /**
     * Lấy danh sách khung giờ (slots) còn trống của giảng viên trong ngày.
     *
     * KHUNG GIỜ CÓ ĐỊNH:
     * - 08:00-09:00, 09:00-10:00, 10:00-11:00, 11:00-12:00 (buổi sáng)
     * - 13:00-14:00, 14:00-15:00, 15:00-16:00, 16:00-17:00 (buổi chiều)
     * - BREAK: 12:00-13:00 (nghỉ trưa)
     *
     * FLOW:
     * 1. Tạo danh sách 8 khung giờ cố định
     * 2. Với mỗi khung, kiểm tra xung đột:
     *    - Gọi sessionRepository.existsConflict() để check có buổi nào overlapping
     *    - Nếu có overlap -> slot đó không khả dụng (available=false)
     * 3. Trả về danh sách slots có flag available
     *
     * CONFLICT CHECK LOGIC:
     * - Kiểm tra slot chứa buổi nào trong cùng ngày + lecturer của giảng viên
     * - Loại exclude CANCELLED, REJECTED, CANCELED_BY_STUDENT
     * - Public: start_time < other.endTime AND end_time > other.startTime
     *
     * PERFORMANCE:
     * - 8 slot x 1 query mỗi cái = 8 queries/request (acceptable vì DB indexed)
     * - Có thể tối ưu bằng 1 query get all conflicts + filter in memory (future)
     *
     * @param lecturerId ID giảng viên
     * @param date Ngày cần check (ví dụ: 2025-05-25)
     * @return List<SlotDto> danh sách slots với available flag
     */
    public List<SlotDto> getAvailableSlots(Long lecturerId, LocalDate date) {
        // Định nghĩa khung giờ cố định (8 slots)
        List<LocalTime[]> allSlots = List.of(
            new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(9, 0)},      // 08:00-09:00
            new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(10, 0)},     // 09:00-10:00
            new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(11, 0)},    // 10:00-11:00
            new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(12, 0)},    // 11:00-12:00
            // BREAK: 12:00-13:00
            new LocalTime[]{LocalTime.of(13, 0), LocalTime.of(14, 0)},    // 13:00-14:00
            new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(15, 0)},    // 14:00-15:00
            new LocalTime[]{LocalTime.of(15, 0), LocalTime.of(16, 0)},    // 15:00-16:00
            new LocalTime[]{LocalTime.of(16, 0), LocalTime.of(17, 0)}     // 16:00-17:00
        );

        List<SlotDto> slots = new ArrayList<>();
        for (LocalTime[] slot : allSlots) {
            SlotDto dto = new SlotDto();
            dto.setStartTime(slot[0]);
            dto.setEndTime(slot[1]);

            // KIỂM TRA XUNG ĐỘT: Có buổi nào overlap với khung giờ này không?
            boolean conflict = sessionRepository.existsConflict(
                lecturerId,
                date,
                slot[0],
                slot[1]
            );

            // Nếu có conflict -> vòng giờ này không khả dụng (available = false)
            dto.setAvailable(!conflict);
            slots.add(dto);
        }
        return slots;
    }

    /**
     * Tạo phiếu đặt lịch cố vấn mới.
     *
     * LUỒNG XỰ LÝ:
     * 1. VALIDATE INPUT:
     *    - Kiểm tra endTime > startTime (lỗi thông thường từ form)
     *    - Kiểm tra ngày/giờ không ở quá khứ (kết hợp date + start_time)
     *
     * 2. CHECK CONFLICT:
     *    - Gọi existsConflict() để xác định giờ đó giảng viên đã được đặt
     *    - Nếu có xung đột -> throw SlotConflictException
     *
     * 3. LOAD & VALIDATE USERS:
     *    - Load Student từ email (Spring Security cung cấp)
     *    - Load Lecturer từ ID, kiểm tra role = LECTURER
     *    - Nếu không đúng role -> throw BusinessException (tránh user bình thường)
     *
     * 4. CREATE ENTITY:
     *    - Tạo MentoringSession với student, lecturer, thời gian, status=PENDING
     *    - Save vào database
     *
     * 5. RETURN:
     *    - Trả về Session object (ID được Spring Data JPA gán)
     *    - Controller có thể gửi response + flash message
     *
     * TRANSACTION:
     * - @Transactional (write): Nếu Exception -> rollback toàn bộ
     * - Nếu save thất bại -> lỗi constraint (unique, null, v.v.)
     *
     * EXCEPTION HANDLING:
     * - BusinessException: Thời gian không hợp lệ, role sai
     * - SlotConflictException: Giá giờ đã bị đặt
     * - ResourceNotFoundException: Không tìm thấy lecturer/student
     *
     * @param dto BookingRequestDto chứa: lecturerId, date, startTime, endTime, note
     * @param studentEmail Email người dùng hiện tại (từ Principal)
     * @return MentoringSession vừa tạo
     * @throws BusinessException nếu validate fail
     * @throws SlotConflictException nếu xung đột giờ
     * @throws ResourceNotFoundException nếu not found
     */
    @Transactional
    public MentoringSession createBooking(BookingRequestDto dto, String studentEmail) {
        // === KIỂM TRA 1: VALIDATE THỜI GIAN ===
        // Bug #32: Validate start < end trước khi lưu
        if (dto.getStartTime() != null && dto.getEndTime() != null
                && !dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BusinessException("Giờ kết thúc phải sau giờ bắt đầu");
        }

        // === KIỂM TRA 2: KHÔNG ĐẶT LỊCH QUÁKHỨ ===
        // Tổng hợp date + startTime -> LocalDateTime để so sánh với hiện tại
        LocalDateTime bookingDateTime = LocalDateTime.of(dto.getSessionDate(), dto.getStartTime());
        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Không thể đặt lịch trong quá khứ");
        }

        // === KIỂM TRA 3: XUNG ĐỘT LỊCHạ ===
        // Gọi Repository query: Có buổi nào của lecturer overlapping không?
        if (sessionRepository.existsConflict(
            dto.getLecturerId(),
            dto.getSessionDate(),
            dto.getStartTime(),
            dto.getEndTime()
        )) {
            throw new SlotConflictException("Khung giờ này đã có lịch");
        }

        // === KIỂM TRA 4: LOAD STUDENT ===
        // Spring Security cung cấp studentEmail từ Principal
        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên"));

        // === KIỂM TRA 5: LOAD & VERIFY LECTURER ===
        // Bug #10: SECURITY RISK - Nếu không kiểm tra role, attacker có thể:
        // - Lấy ID bất kỳ User (thậm chí ADMIN)
        // - Tạo Session với Account sai
        // -> ĐỦI ĐÔI KIỂM TRA user.role == LECTURER
        User lecturer = userRepository.findById(dto.getLecturerId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        if (lecturer.getRole() != UserRole.LECTURER) {
            throw new BusinessException("ID được cung cấp không phải là giảng viên");
        }

        // === BƯỚC CUỐI: TẠO & LƯU SESSION ===
        MentoringSession session = new MentoringSession();
        session.setStudent(student);
        session.setLecturer(lecturer);
        session.setSessionDate(dto.getSessionDate());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setNote(dto.getNote());
        session.setStatus(SessionStatus.PENDING);  // Trạng thái mặc định

        // Spring Data JPA tự generate INSERT query + set ID trả về
        return sessionRepository.save(session);
        }
    }


