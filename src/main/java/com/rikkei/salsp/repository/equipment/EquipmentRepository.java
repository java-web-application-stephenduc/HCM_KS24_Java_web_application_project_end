package com.rikkei.salsp.repository.equipment;

import com.rikkei.salsp.entity.equipment.Equipment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

/**
 * Repository quản lý truy vấn dữ liệu Equipment từ database.
 *
 * TRÁCH NHIỆ: Cung cấp các query:
 * - Lấy thiết bị không bị xóa (soft delete)
 * - Tìm kiếm theo tên
 * - Lock thiết bị khi cập nhật quantity (tránh race condition)
 * - Tìm thiết bị tồn kho thấp (cảnh báo cần nhập thêm)
 *
 * CONCURRENCY CONTROL:
 * - Khi 2+ sinh viên mượn cùng lúc, cần lock để tránh data corruption
 * - Sử dụng @Lock(LockModeType.PESSIMISTIC_WRITE)
 */
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /**
     * Lấy danh sách tất cả thiết bị chưa bị xóa (soft delete).
     *
     * USE CASE:
     * - Hiển thị danh sách thiết bị khả dụng cho sinh viên
     * - Danh sách quản lý kho cho Admin
     *
     * BUSINESS RULE:
     * - Chỉ lấy deleted = false (loại bỏ thiết bị đã hỏng/loại bỏ)
     * - Không phân trang (sử dụng khi số lượng thiết bị nhỏ)
     *
     * @return List<Equipment> danh sách thiết bị còn hoạt động
     */
    List<Equipment> findByDeletedFalse();

    /**
     * Tìm kiếm thiết bị theo tên (fuzzy search).
     *
     * USE CASE:
     * - Sinh viên tìm "Oscilloscope" trên UI
     * - Admin tìm thiết bị theo keyword
     *
     * QUERY DETAILS:
     * - findByNameContainingIgnoreCaseAndDeletedFalse(String keyword)
     * - "Containing": Contains keyword (ví dụ: "oscilloscope" = "Oscilloscope" ✓)
     * - "IgnoreCase": Case-insensitive (tìm kiếm không phân biệt hoa/thường)
     * - "And": Phải thỏa 2 điều kiện: name chứa keyword VÀ deleted = false
     *
     * HIỆU NĂNG:
     * - Spring Data JPA tự generate query: SELECT * FROM equipments WHERE name LIKE ? AND deleted = false
     * - Index nên được tạo trên (name, deleted) cột để tối ưu
     *
     * @param keyword Từ khóa tìm kiếm (ví dụ: "osc" -> "Oscilloscope")
     * @return List<Equipment> danh sách thiết bị khớp
     */
    List<Equipment> findByNameContainingIgnoreCaseAndDeletedFalse(String keyword);

    /**
     * Lấy thiết bị với PESSIMISTIC_WRITE lock để cập nhật quantity an toàn.
     *
     * MỤC ĐÍCH:
     * - Khi sinh viên mượn thiết bị, cần update quantityAvailable
     * - Nếu 2 request cùng lúc cập nhật -> race condition -> dữ liệu sai
     * - Giải pháp: Lock dòng (row) cho đến khi transaction commit
     *
     * CONCURRENCY CONTROL - PESSIMISTIC LOCKING:
     * - @Lock(LockModeType.PESSIMISTIC_WRITE): Khóa dòng (row) ở database level
     * - Khi request A lock, request B phải chờ cho đến khi A release
     * - Release tự động khi transaction commit/rollback
     *
     * FLOW:
     * 1. Request A: findByIdWithLock(5) -> Khóa Equipment id=5
     * 2. Request B: findByIdWithLock(5) -> Chờ A release (blocking)
     * 3. Request A: quantityAvailable -= qty_request; save() -> Commit
     * 4. Request B: Chiếm lock, đọc dữ liệu mới, update...
     *
     * LƯU Ý:
     * - Phải trong @Transactional context để lock có hiệu lực
     * - Deadlock risk nếu locking order không nhất quán (cần cẩn thận)
     * - Timeout có thể set ở database/application config
     *
     * @param id Equipment ID
     * @return Optional<Equipment> với lock PESSIMISTIC_WRITE
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id = :id")
    Optional<Equipment> findByIdWithLock(@Param("id") Long id);

    /**
     * Tìm danh sách thiết bị có tồn kho thấp (dưới threshold).
     *
     * USE CASE:
     * - Cảnh báo kho cho Admin: "Cần nhập thêm Oscilloscope"
     * - Báo cáo tồn kho thấp hằng tháng
     *
     * QUERY DETAILS:
     * - Chỉ lọc thiết bị không bị xóa (deleted = false)
     * - Tìm thiết bị có quantityAvailable <= threshold
     * - Ví dụ: findLowStock(2) -> lấy thiết bị có ít hơn 2 cái
     *
     * HIỆU NĂNG:
     * - Sử dụng @Query (custom SQL) để rõ ràng
     * - Index nên tạo trên (deleted, quantityAvailable)
     *
     * @param threshold Ngưỡng (ví dụ: 2 = cảnh báo khi < 2 cái còn lại)
     * @return List<Equipment> danh sách thiết bị tồn kho thấp
     */
    @Query("SELECT e FROM Equipment e WHERE e.deleted = false AND e.quantityAvailable <= :threshold")
    List<Equipment> findLowStock(@Param("threshold") int threshold);
}
