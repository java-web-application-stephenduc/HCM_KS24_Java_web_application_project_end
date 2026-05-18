package com.rikkei.salsp.repository;

import com.rikkei.salsp.entity.BorrowingDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowingDetailRepository extends JpaRepository<BorrowingDetail, Long> {
    List<BorrowingDetail> findByRecordId(Long recordId);

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


