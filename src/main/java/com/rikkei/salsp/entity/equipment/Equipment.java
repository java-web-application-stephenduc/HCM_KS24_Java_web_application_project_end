package com.rikkei.salsp.entity.equipment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Thực thể Equipment đại diện cho thông tin một loại thiết bị trong kho phòng Lab.
 *
 * MỤC ĐÍCH: Lưu trữ master data về thiết bị (tên, mô tả, số lượng tồn kho).
 *
 * QUAN TRỌNG - INVENTORY MANAGEMENT:
 * - quantityTotal: Tổng số lượng thiết bị mua vào (không bao giờ giảm)
 * - quantityAvailable: Số lượng hiện tại khả dụng (giảm khi sinh viên mượn, tăng khi trả)
 * - Công thức: quantityBorrowed = quantityTotal - quantityAvailable
 *
 * SOFT DELETE:
 * - deleted = false: Thiết bị còn sử dụng
 * - deleted = true: Thiết bị đã hỏng/loại bỏ (không hiển thị trong danh sách)
 * - Tất cả query phải kiểm tra "WHERE deleted = false"
 *
 * OPTIMISTIC LOCKING:
 * - quantityAvailable có thể được update bởi nhiều request đồng thời (N sinh viên mượn)
 * - Kiểm soát với @Lock(LockModeType.PESSIMISTIC_WRITE) ở Repository
 * - Tránh race condition: 2 sinh viên mượn cùng lúc làm dữ liệu sai
 */
@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Tên thiết bị (ví dụ: "Máy Oscilloscope", "Multimeter", "Soldering Iron").
     * Dùng để hiển thị trên UI, tìm kiếm, lọc.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Mô tả chi tiết về thiết bị (thông số kỹ thuật, hướng dẫn sử dụng, v.v.).
     * Lưu dạng TEXT (unlimited length) để chứa description dài.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Danh mục / Loại thiết bị (ví dụ: "Điện tử", "Cơ khí", "Hóa học").
     * Dùng để phân loại, filter thiết bị khi hiển thị.
     */
    @Column(length = 100)
    private String category;

    /**
     * Tổng số lượng thiết bị đã mua vào (master data - không bao giờ thay đổi).
     * Ví dụ: quantityTotal = 5 nghĩa là mua 5 cái của loại thiết bị này.
     * Dùng để tính toán:
     * - Tỷ lệ hỏng hóc: (quantityTotal - quantityAvailable) / quantityTotal
     * - Báo cáo: Bao nhiêu cái Oscilloscope hiện có / đã mua
     */
    @Column(name = "quantity_total", nullable = false)
    private int quantityTotal;

    /**
     * Số lượng hiện tại khả dụng (động, thay đổi khi mượn/trả).
     * Ví dụ: nếu quantityAvailable = 3, có 3 cái có thể mượn ngay.
     *
     * LUỒNG CẬP NHẬT:
     * 1. Sinh viên đặt mượn -> quantityAvailable -= quantity_requested (trong transaction)
     * 2. Admin phê duyệt -> tạo BorrowingRecord
     * 3. Sinh viên trả -> quantityAvailable += quantity_returned
     *
     * KIỂM TRA:
     * - Quá trình mượn phải kiểm tra: quantityAvailable >= quantity_requested
     * - Nếu không đủ -> throw InsufficientStockException
     */
    @Column(name = "quantity_available", nullable = false)
    private int quantityAvailable;

    /**
     * Soft delete flag - đánh dấu thiết bị đã ngừng sử dụng.
     *
     * deleted = false: Thiết bị còn hoạt động (mặc định)
     * deleted = true: Thiết bị hủy (không hiển thị, không cho mượn)
     *
     * LỢI THẾ:
     * - Giữ lịch sử (report thống kê vẫn có dữ liệu cũ)
     * - Không gây lỗi FK constraint
     * - Mềm dẻo hơn physical delete
     *
     * LƯU Ý:
     * - Tất cả query phải kiểm tra: WHERE deleted = false
     * - Ở Repository có method findByDeletedFalse() đã handle
     */
    @Column(nullable = false)
    private boolean deleted = false;

    /**
     * Dấu thời gian tạo bản ghi.
     * @CreationTimestamp tự động set khi INSERT, không thể UPDATE.
     * Dùng cho audit trail (kiểm soát lịch sử).
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Dấu thời gian cập nhật gần nhất.
     * @UpdateTimestamp tự động update khi thực hiện UPDATE.
     * Dùng để check: thiết bị vừa được cập nhật khi nào?
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

