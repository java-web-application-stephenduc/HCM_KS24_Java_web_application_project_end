package com.rikkei.salsp.service.lecturer;

import com.rikkei.salsp.dto.lecturer.LecturerDashboardDto;
import com.rikkei.salsp.dto.lecturer.LecturerDashboardDto.PendingStudentDto;
import com.rikkei.salsp.dto.lecturer.LecturerDashboardDto.TodayAppointmentDto;
import com.rikkei.salsp.entity.session.MentoringSession;
import com.rikkei.salsp.entity.session.SessionStatus;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.session.MentoringSessionRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service cung cấp thống kê số lượng ca hướng dẫn và danh sách ca sắp diễn ra cho Lecturer Dashboard.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LecturerDashboardService {

    private final MentoringSessionRepository sessionRepository;
    private final UserRepository userRepository;

    /**
     * Lấy thông tin bảng điều khiển.
     * @param lecturerEmail Tham số đầu vào lecturerEmail

     * @return Kết quả trả về của phương thức
     */
    public LecturerDashboardDto getDashboardData(String lecturerEmail) {
        User lecturer = userRepository.findByEmail(lecturerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        Long lecturerId = lecturer.getId();

        LecturerDashboardDto dto = new LecturerDashboardDto();

        LocalDate today = LocalDate.now();
        List<MentoringSession> todaySessions = sessionRepository
            .findByLecturerIdAndDateWithStudent(lecturerId, today);
        dto.setTodayCount(todaySessions.size());

        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        long monthlyCount = sessionRepository
            .countByLecturerIdAndDateBetween(lecturerId, startOfMonth, endOfMonth);
        dto.setMonthlyCount(monthlyCount);

        List<MentoringSession> pendingSessions = sessionRepository
            .findQueueWithStudent(lecturerId, SessionStatus.PENDING);
        List<PendingStudentDto> pendingStudents = pendingSessions.stream()
            .map(s -> {
                PendingStudentDto ps = new PendingStudentDto();
                ps.setSessionId(s.getId());
                ps.setStudentName(s.getStudent() != null && s.getStudent().getProfile() != null
                    ? s.getStudent().getProfile().getFullName() : "Sinh viên");
                ps.setStudentCode(s.getStudent() != null ? s.getStudent().getEmail() : "");
                ps.setReason(s.getNote());
                ps.setRequestDate(s.getSessionDate());
                return ps;
            })
            .collect(Collectors.toList());
        dto.setPendingStudents(pendingStudents);
        dto.setPendingCount(pendingStudents.size());

        List<TodayAppointmentDto> appointments = todaySessions.stream()
            .map(s -> {
                TodayAppointmentDto a = new TodayAppointmentDto();
                a.setStudentName(s.getStudent() != null && s.getStudent().getProfile() != null
                    ? s.getStudent().getProfile().getFullName() : "Sinh viên");
                a.setTime(s.getStartTime());
                a.setLocation("Phòng tư vấn");
                a.setOnline(false);
                return a;
            })
            .collect(Collectors.toList());
        dto.setTodayAppointments(appointments);

        return dto;
    }
}
