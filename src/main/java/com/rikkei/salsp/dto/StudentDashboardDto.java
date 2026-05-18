package com.rikkei.salsp.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentDashboardDto {

    private long upcomingCount;
    private long completedCount;
    private long borrowedCount;
    private String studentName;
    private RecentSessionDto latestSession;
    private List<RecentSessionDto> recentSessions;
    private List<RecentEquipmentDto> recentEquipments;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RecentSessionDto {
        private Long sessionId;
        private String lecturerName;
        private String lecturerTitle;
        private LocalDate sessionDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RecentEquipmentDto {
        private String equipmentName;
        private String borrowStatus;
    }
}
