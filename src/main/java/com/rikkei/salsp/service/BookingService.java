package com.rikkei.salsp.service;

import com.rikkei.salsp.dto.BookingRequestDto;
import com.rikkei.salsp.dto.DepartmentDto;
import com.rikkei.salsp.dto.LecturerSummaryDto;
import com.rikkei.salsp.dto.SlotDto;
import com.rikkei.salsp.entity.MentoringSession;
import com.rikkei.salsp.entity.SessionStatus;
import com.rikkei.salsp.entity.User;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.exception.SlotConflictException;
import com.rikkei.salsp.repository.DepartmentRepository;
import com.rikkei.salsp.repository.LecturerRepository;
import com.rikkei.salsp.repository.MentoringSessionRepository;
import com.rikkei.salsp.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;
    private final MentoringSessionRepository sessionRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public MentoringSession createBooking(BookingRequestDto dto, String studentEmail) {
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
        User lecturer = userRepository.findById(dto.getLecturerId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

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


