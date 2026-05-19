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
 * Service quản lý quy trình đặt lịch cố vấn, gợi ý khung giờ trống và kiểm tra xung đột tài nguyên/lịch hẹn.
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
     * Lấy thông tin khoa/ban chuyên môn.

     * @return Kết quả trả về của phương thức
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
     * Lấy thông tin giảng viên.
     * @param departmentId Tham số đầu vào departmentId

     * @return Kết quả trả về của phương thức
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
     * Lấy thông tin giảng viên theo ID tài khoản để hiển thị ở bước 3.
     *
     * @param lecturerId ID tài khoản giảng viên
     * @return Thông tin giảng viên
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
     * Lấy danh sách các khung giờ (slots) cố vấn còn trống của một giảng viên trong một ngày cụ thể.
     * @param lecturerId Tham số đầu vào lecturerId
     * @param date Tham số đầu vào date

     * @return Kết quả trả về của phương thức
     */
    public List<SlotDto> getAvailableSlots(Long lecturerId, LocalDate date) {
        // Tạo khung giờ cố định và đánh dấu trống theo kiểm tra xung đột.
        List<LocalTime[]> allSlots = List.of(
            new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(9, 0)},
            new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(10, 0)},
            new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(11, 0)},
            new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(12, 0)},
            new LocalTime[]{LocalTime.of(13, 0), LocalTime.of(14, 0)},
            new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(15, 0)},
            new LocalTime[]{LocalTime.of(15, 0), LocalTime.of(16, 0)},
            new LocalTime[]{LocalTime.of(16, 0), LocalTime.of(17, 0)}
        );

        List<SlotDto> slots = new ArrayList<>();
        for (LocalTime[] slot : allSlots) {
            SlotDto dto = new SlotDto();
            dto.setStartTime(slot[0]);
            dto.setEndTime(slot[1]);
            boolean conflict = sessionRepository.existsConflict(lecturerId, date, slot[0], slot[1]);
            dto.setAvailable(!conflict);
            slots.add(dto);
        }
        return slots;
    }

    /**
     * Thực hiện đặt lịch hẹn cố vấn học tập mới. Kiểm tra chặn ngày quá khứ và chặn trùng lịch (conflict check) trước khi lưu.

     * <p><strong>Lưu ý sửa lỗi (Bug #32: Validate thời gian hợp lệ trước khi lưu.)</strong></p>
     * @param dto Tham số đầu vào dto
     * @param studentEmail Tham số đầu vào studentEmail

     * @return Kết quả trả về của phương thức
     */
    @Transactional
    public MentoringSession createBooking(BookingRequestDto dto, String studentEmail) {
        // Bug #32: Validate thời gian hợp lệ trước khi lưu.
        if (dto.getStartTime() != null && dto.getEndTime() != null
                && !dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BusinessException("Giờ kết thúc phải sau giờ bắt đầu");
        }

        // Kiểm tra thời gian không ở quá khứ.
        LocalDateTime bookingDateTime = LocalDateTime.of(dto.getSessionDate(), dto.getStartTime());
        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Không thể đặt lịch trong quá khứ");
        }

        // Kiểm tra xung đột lịch của giảng viên.
        if (sessionRepository.existsConflict(dto.getLecturerId(), dto.getSessionDate(),
            dto.getStartTime(), dto.getEndTime())) {
            throw new SlotConflictException("Khung giờ này đã có lịch");
        }

        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên"));

        // Bug #10: Verify lecturerId thuộc về tài khoản có role LECTURER.
        // Nếu không kiểm tra, hàm userRepository.findById có thể trả về bất kỳ User nào (STUDENT/ADMIN)
        // khiến session được gán sai giảng viên.
        User lecturer = userRepository.findById(dto.getLecturerId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        if (lecturer.getRole() != UserRole.LECTURER) {
            throw new BusinessException("ID được cung cấp không phải là giảng viên");
        }

        MentoringSession session = new MentoringSession();
        session.setStudent(student);
        session.setLecturer(lecturer);
        session.setSessionDate(dto.getSessionDate());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setNote(dto.getNote());
        session.setStatus(SessionStatus.PENDING);
        return sessionRepository.save(session);
    }
}


