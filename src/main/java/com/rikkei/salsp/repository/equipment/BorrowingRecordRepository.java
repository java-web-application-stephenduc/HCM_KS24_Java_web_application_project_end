package com.rikkei.salsp.repository.equipment;

import com.rikkei.salsp.entity.equipment.BorrowingRecord;
import com.rikkei.salsp.entity.equipment.BorrowingStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository quản lý truy vấn dữ liệu thực thể BorrowingRecord.
 */
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {
    @Override
    @EntityGraph(attributePaths = {
        "session", "session.student", "session.student.profile", "details", "details.equipment"
    })
    Page<BorrowingRecord> findAll(Pageable pageable);

    /**
     * Tìm kiếm buổi cố vấn học thuật.
     * 
     * @param sessionId Tham số đầu vào sessionId
     * 
     * @return Kết quả trả về của phương thức
     */
    Optional<BorrowingRecord> findBySessionId(Long sessionId);

    /**
     * Tìm kiếm.
     * 
     * @param status Tham số đầu vào status
     * 
     * @return Kết quả trả về của phương thức
     */
    List<BorrowingRecord> findByStatus(BorrowingStatus status);

    @Query("""
                SELECT DISTINCT br FROM BorrowingRecord br
                LEFT JOIN FETCH br.session s
                LEFT JOIN FETCH s.student st
                LEFT JOIN FETCH st.profile p
                LEFT JOIN FETCH br.details d
                LEFT JOIN FETCH d.equipment e
                WHERE br.status = :status
            """)
    List<BorrowingRecord> findByStatusWithDetails(@Param("status") BorrowingStatus status);

    @Query("""
                SELECT DISTINCT br FROM BorrowingRecord br
                LEFT JOIN FETCH br.session s
                LEFT JOIN FETCH s.student st
                LEFT JOIN FETCH st.profile p
                LEFT JOIN FETCH br.details d
                LEFT JOIN FETCH d.equipment e
                WHERE br.status IN :statuses
            """)
    List<BorrowingRecord> findByStatusInWithDetails(@Param("statuses") List<BorrowingStatus> statuses);

    @Query("""
                SELECT br FROM BorrowingRecord br
                LEFT JOIN FETCH br.session s
                LEFT JOIN FETCH s.student st
                LEFT JOIN FETCH st.profile p
                WHERE br.id = :id
            """)
    Optional<BorrowingRecord> findByIdWithAssociations(@Param("id") Long id);

    @Query("""
                SELECT br FROM BorrowingRecord br
                LEFT JOIN FETCH br.details d
                LEFT JOIN FETCH d.equipment e
                WHERE br.session.id = :sessionId
            """)
    Optional<BorrowingRecord> findBySessionIdWithDetails(@Param("sessionId") Long sessionId);

    /**
     * Đếm số lượng.
     * 
     * @param status Tham số đầu vào status
     * 
     * @return Kết quả trả về của phương thức
     */
    long countByStatus(BorrowingStatus status);

    @Query("SELECT COUNT(DISTINCT br.id) FROM BorrowingRecord br WHERE br.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<BorrowingStatus> statuses);

    @Modifying
    @Query("""
        UPDATE BorrowingRecord br
        SET br.status = com.rikkei.salsp.entity.equipment.BorrowingStatus.OVERDUE
        WHERE br.status = com.rikkei.salsp.entity.equipment.BorrowingStatus.DISPATCHED
          AND br.session.sessionDate < :today
    """)
    int markDispatchedAsOverdue(@Param("today") LocalDate today);
}
