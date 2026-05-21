package com.rikkei.salsp.dto.student;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa dữ liệu cá nhân dashboard sinh viên.
 *
 * MỤC ĐÍCH: Tổng hợp các KPI (Key Performance Indicators) riêng của sinh viên.
 *
 * CÁC METRICS:
 * - upcomingCount: Số buổi cố vấn sắp tới (PENDING hoặc CONFIRMED)
 * - completedCount: Số buổi hoàn thành (COMPLETED)
 * - borrowedCount: Số phiếu mượn hiện đang active (BORROWED)
 * - studentName: Tên hiển thị của sinh viên
 * - latestSession: Buổi gần nhất (để hiển thị "Buổi tiếp theo")
 * - recentSessions: Danh sách 5-10 buổi gần nhất
 * - recentEquipments: Danh sách thiết bị vừa mượn
 *
 * FLOW:
 * Student login → GET /student/dashboard
 * → StudentDashboardService.getDashboardData(studentId)
 * → Collect từ 3-4 queries (MentoringSession, BorrowingRecord)
 * → Build DTO → Render Thymeleaf template
 *
 * UI USAGE:
 * - Hiển thị card: "X buổi sắp tới", "Y buổi hoàn thành"
 * - Quick links: Xem buổi tiếp theo, mượn thiết bị lần cuối
 */
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
