package com.rikkei.salsp.dto.lecturer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `LecturerDashboardDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class LecturerDashboardDto {

    private long pendingCount;
    private long todayCount;
    private long monthlyCount;
    private List<PendingStudentDto> pendingStudents = new ArrayList<>();
    private List<TodayAppointmentDto> todayAppointments = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PendingStudentDto {
        private Long sessionId;
        private String studentName;
        private String studentCode;
        private String department;
        private String reason;
        private LocalDate requestDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TodayAppointmentDto {
        private String studentName;
        private LocalTime time;
        private String location;
        private boolean online;
    }
}
