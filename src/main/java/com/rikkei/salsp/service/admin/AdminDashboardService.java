package com.rikkei.salsp.service.admin;

import com.rikkei.salsp.dto.admin.AdminDashboardDto;
import com.rikkei.salsp.dto.admin.AdminDashboardDto.LowStockEquipmentDto;
import com.rikkei.salsp.dto.admin.AdminDashboardDto.MonthlyStatDto;
import com.rikkei.salsp.dto.admin.AdminDashboardDto.TopLecturerDto;
import com.rikkei.salsp.entity.equipment.BorrowingStatus;
import com.rikkei.salsp.entity.equipment.Equipment;
import com.rikkei.salsp.entity.session.SessionStatus;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserRole;
import com.rikkei.salsp.repository.equipment.BorrowingRecordRepository;
import com.rikkei.salsp.repository.equipment.EquipmentRepository;
import com.rikkei.salsp.repository.session.MentoringSessionRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service cung cấp dữ liệu thống kê tổng hợp và các chỉ số hoạt động cho màn hình Admin Dashboard.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final MentoringSessionRepository sessionRepository;
    private final UserRepository userRepository;

    private static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Lấy thông tin bảng điều khiển.

     * @return Kết quả trả về của phương thức
     */
    public AdminDashboardDto getDashboardData() {
        AdminDashboardDto dto = new AdminDashboardDto();

        long borrowedCount = borrowingRecordRepository.countByStatus(BorrowingStatus.DISPATCHED);
        dto.setBorrowedCount(borrowedCount);

        long pendingDispatchCount = borrowingRecordRepository.countByStatus(BorrowingStatus.PENDING_DISPATCH);
        dto.setPendingDispatchCount(pendingDispatchCount);

        long completedCount = sessionRepository.countByStatus(SessionStatus.COMPLETED);
        dto.setCompletedCount(completedCount);

        List<Equipment> lowStockItems = equipmentRepository.findLowStock(LOW_STOCK_THRESHOLD);
        dto.setLowStockCount(lowStockItems.size());

        List<LowStockEquipmentDto> lowStockDtos = new ArrayList<>();
        int idx = 1;
        for (Equipment eq : lowStockItems) {
            LowStockEquipmentDto ls = new LowStockEquipmentDto();
            ls.setCode(String.format("TB-%03d", eq.getId()));
            ls.setName(eq.getName());
            ls.setQuantity(eq.getQuantityAvailable());
            lowStockDtos.add(ls);
        }
        dto.setLowStockEquipments(lowStockDtos);

        List<MonthlyStatDto> monthlyStats = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate target = now.minusMonths(i);
            int m = target.getMonthValue();
            int y = target.getYear();
            long count = sessionRepository.countCompletedSessionsByMonthAndYear(m, y);
            monthlyStats.add(createMonthlyStat("T" + m, (int) count));
        }
        dto.setMonthlyStats(monthlyStats);

        List<TopLecturerDto> topLecturers = new ArrayList<>();
        List<Object[]> raw = sessionRepository.findTopLecturers(PageRequest.of(0, 5));
        if (raw.isEmpty()) {
            List<User> lecturers = userRepository.findByRole(UserRole.LECTURER, PageRequest.of(0, 5)).getContent();
            for (User lec : lecturers) {
                String name = (lec.getProfile() != null) ? lec.getProfile().getFullName() : lec.getEmail();
                topLecturers.add(createTopLecturer(name, 0, 0));
            }
        } else {
            long maxCount = raw.isEmpty() ? 1 : ((Number) raw.get(0)[1]).longValue();
            for (Object[] row : raw) {
                Long lecturerId = (Long) row[0];
                long cnt = ((Number) row[1]).longValue();
                User user = userRepository.findById(lecturerId).orElse(null);
                String name = (user != null && user.getProfile() != null)
                    ? user.getProfile().getFullName() : "Giảng viên #" + lecturerId;
                int percent = maxCount > 0 ? (int) (cnt * 100 / maxCount) : 0;
                topLecturers.add(createTopLecturer(name, (int) cnt, percent));
            }
        }
        dto.setTopLecturers(topLecturers);

        return dto;
    }

    /**
     * Tạo mới.
     * @param month Tham số đầu vào month
     * @param count Tham số đầu vào count

     * @return Kết quả trả về của phương thức
     */
    private MonthlyStatDto createMonthlyStat(String month, int count) {
        MonthlyStatDto dto = new MonthlyStatDto();
        dto.setMonth(month);
        dto.setCount(count);
        return dto;
    }

    /**
     * Tạo mới giảng viên.
     * @param name Tham số đầu vào name
     * @param count Tham số đầu vào count
     * @param percent Tham số đầu vào percent

     * @return Kết quả trả về của phương thức
     */
    private TopLecturerDto createTopLecturer(String name, int count, int percent) {
        TopLecturerDto dto = new TopLecturerDto();
        dto.setName(name);
        dto.setCount(count);
        dto.setPercent(percent);
        return dto;
    }
}
