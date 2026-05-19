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
 * Repository quản lý truy vấn dữ liệu thực thể Equipment.
 */
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    /**
     * Tìm kiếm.

     * @return Kết quả trả về của phương thức
     */
    List<Equipment> findByDeletedFalse();

    /**
     * Tìm kiếm.
     * @param keyword Tham số đầu vào keyword

     * @return Kết quả trả về của phương thức
     */
    List<Equipment> findByNameContainingIgnoreCaseAndDeletedFalse(String keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id = :id")
    Optional<Equipment> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT e FROM Equipment e WHERE e.deleted = false AND e.quantityAvailable <= :threshold")
    List<Equipment> findLowStock(@Param("threshold") int threshold);
}
