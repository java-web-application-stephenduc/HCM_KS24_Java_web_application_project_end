package com.rikkei.salsp.service;

import com.rikkei.salsp.dto.StudentDashboardDto;
import com.rikkei.salsp.dto.StudentDashboardDto.RecentEquipmentDto;
import com.rikkei.salsp.dto.StudentDashboardDto.RecentSessionDto;
import com.rikkei.salsp.entity.BorrowingDetail;
import com.rikkei.salsp.entity.BorrowingStatus;
import com.rikkei.salsp.entity.MentoringSession;
import com.rikkei.salsp.entity.SessionStatus;
import com.rikkei.salsp.entity.User;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.BorrowingDetailRepository;
import com.rikkei.salsp.repository.MentoringSessionRepository;
import com.rikkei.salsp.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentDashboardService {

    private final MentoringSessionRepository sessionRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final UserRepository userRepository;

    /**
     * Tổng hợp dữ liệu hiển thị cho dashboard sinh viên.
     *
     * @param studentId ID sinh viên
     * @return Dữ liệu dashboard
     */
    public StudentDashboardDto getDashboardData(Long studentId) {
        StudentDashboardDto dto = new StudentDashboardDto();

        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên"));
        dto.setStudentName(student.getProfile() != null ? student.getProfile().getFullName() : "Sinh viên");

        long upcoming = sessionRepository.countByStudentIdAndStatusIn(studentId,
            List.of(SessionStatus.PENDING, SessionStatus.CONFIRMED));
        long completed = sessionRepository.countByStudentIdAndStatusIn(studentId,
            List.of(SessionStatus.COMPLETED));

        dto.setUpcomingCount(upcoming);
        dto.setCompletedCount(completed);

        List<MentoringSession> sessions = sessionRepository.findByStudentIdWithLecturerProfile(studentId);
        if (sessions.size() > 5) sessions = sessions.subList(0, 5);

        List<RecentSessionDto> recent = sessions.stream().map(this::toRecentSession).collect(Collectors.toList());
        dto.setRecentSessions(recent);
        if (!recent.isEmpty()) {
            dto.setLatestSession(recent.get(0));
        }

        List<BorrowingDetail> details = borrowingDetailRepository.findByStudentIdWithEquipment(studentId);
        long borrowedCount = details.stream()
            .filter(d -> d.getRecord().getStatus() == BorrowingStatus.DISPATCHED
                      || d.getRecord().getStatus() == BorrowingStatus.OVERDUE)
            .count();
        dto.setBorrowedCount(borrowedCount);

        List<RecentEquipmentDto> eqDtos = new ArrayList<>();
        for (BorrowingDetail detail : details) {
            if (eqDtos.size() >= 5) break;
            RecentEquipmentDto eq = new RecentEquipmentDto();
            eq.setEquipmentName(detail.getEquipment().getName());
            eq.setBorrowStatus(detail.getRecord().getStatus().name());
            eqDtos.add(eq);
        }
        dto.setRecentEquipments(eqDtos);

        return dto;
    }

    private RecentSessionDto toRecentSession(MentoringSession s) {
        RecentSessionDto dto = new RecentSessionDto();
        dto.setSessionId(s.getId());
        dto.setLecturerName(s.getLecturer().getProfile() != null
            ? s.getLecturer().getProfile().getFullName() : "Chưa rõ");
        dto.setSessionDate(s.getSessionDate());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setStatus(s.getStatus().name());
        return dto;
    }
}
