package com.rikkei.salsp.repository.equipment;
import com.rikkei.salsp.entity.equipment.BorrowingStatus;

import com.rikkei.salsp.entity.equipment.BorrowingDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository quản lý truy vấn dữ liệu thực thể BorrowingDetail.
 */
public interface BorrowingDetailRepository extends JpaRepository<BorrowingDetail, Long> {
    /**
     * Tìm kiếm bản ghi.
     * @param recordId Tham số đầu vào recordId

     * @return Kết quả trả về của phương thức
     */
    List<BorrowingDetail> findByRecordId(Long recordId);

    @Query("""
        SELECT bd FROM BorrowingDetail bd
        JOIN FETCH bd.equipment e
        WHERE bd.record.id = :recordId
    """)
    List<BorrowingDetail> findByRecordIdWithEquipment(@Param("recordId") Long recordId);

    @Query("""
        SELECT COUNT(bd) > 0 FROM BorrowingDetail bd
        JOIN bd.record br
        WHERE bd.equipment.id = :equipmentId
          AND br.status IN (com.rikkei.salsp.entity.BorrowingStatus.PENDING_DISPATCH,
                            com.rikkei.salsp.entity.BorrowingStatus.DISPATCHED)
    """)
    boolean existsByEquipmentIdAndActiveStatus(@Param("equipmentId") Long equipmentId);

    @Query("""
        SELECT bd FROM BorrowingDetail bd
        JOIN FETCH bd.record br
        JOIN FETCH br.session s
        JOIN FETCH bd.equipment e
        WHERE s.student.id = :studentId
        ORDER BY br.createdAt DESC
    """)
    List<BorrowingDetail> findByStudentIdWithEquipment(@Param("studentId") Long studentId);
}


