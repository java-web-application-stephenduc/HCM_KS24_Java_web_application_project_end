package com.rikkei.salsp.dto.lecturer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO phản hồi cho Lecturer Dashboard — gồm thống kê tổng quan và danh sách chi tiết.
 * Chứa các lớp tĩnh bên trong cho dữ liệu phụ (pending students, today appointments).
 */
@Getter
@Setter
@NoArgsConstructor
public class LecturerDashboardDto {

    /* Số lượng sinh viên chờ giảng viên xác nhận (SessionStatus.PENDING) */
    private long pendingCount;
    /* Số lịch hẹn trong ngày hôm nay (không bao gồm các buổi đã hủy) */
    private long todayCount;
    /* Tổng số buổi cố vấn trong tháng hiện tại */
    private long monthlyCount;
    /* Danh sách sinh viên đang chờ xử lý (PENDING) */
    private List<PendingStudentDto> pendingStudents = new ArrayList<>();
    /* Danh sách lịch hẹn hôm nay, sắp xếp theo giờ */
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
