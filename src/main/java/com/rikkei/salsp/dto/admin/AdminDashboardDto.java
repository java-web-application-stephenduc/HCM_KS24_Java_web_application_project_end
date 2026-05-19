package com.rikkei.salsp.dto.admin;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `AdminDashboardDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class AdminDashboardDto {

    private long borrowedCount;
    private long pendingDispatchCount;
    private long completedCount;
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
