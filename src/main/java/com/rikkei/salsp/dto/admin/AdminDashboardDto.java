package com.rikkei.salsp.dto.admin;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa dữ liệu thống kê dashboard cho Admin.
 *
 * MỤC ĐÍCH: Tổng hợp các KPI (Key Performance Indicators) của hệ thống.
 *
 * CÁC METRICS:
 * - borrowedCount: Số phiếu mượn hiện đang hoạt động (status = BORROWED)
 * - pendingDispatchCount: Số phiếu chờ phê duyệt xuất kho (status = PENDING)
 * - completedCount: Số buổi cố vấn hoàn thành trong tháng
 * - totalUsers: Tổng số user hệ thống (cả active/inactive)
 * - activeUsers: Số tài khoản đang hoạt động (active = true)
 * - lowStockCount: Số loại thiết bị tồn kho thấp (< threshold)
 *
 * SUB-DTO STRUCTURES:
 * - LowStockEquipmentDto[]: Danh sách chi tiết thiết bị cảnh báo
 * - MonthlyStatDto[]: Biểu đồ xu hướng theo tháng (tháng này, tháng trước, ...)
 * - TopLecturerDto[]: Top 10 giảng viên có buổi COMPLETED nhiều nhất
 *
 * FLOW:
 * AdminDashboardService.getDashboardStats() → collect từ N queries → build DTO
 * → Controller render Thymeleaf template với dữ liệu dashboard
 */
@Getter
@Setter
@NoArgsConstructor
public class AdminDashboardDto {

    private long borrowedCount;
    private long pendingDispatchCount;
    private long completedCount;
    private long totalUsers;
    private long activeUsers;
    private long lowStockCount;
    private List<LowStockEquipmentDto> lowStockEquipments = new ArrayList<>();
    private List<MonthlyStatDto> monthlyStats = new ArrayList<>();
    private List<TopLecturerDto> topLecturers = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LowStockEquipmentDto {
        private String code;
        private String name;
        private int quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MonthlyStatDto {
        private String month;
        private int count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TopLecturerDto {
        private String name;
        private int count;
        private int percent;
    }
}
