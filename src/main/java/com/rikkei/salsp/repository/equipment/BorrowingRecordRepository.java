package com.rikkei.salsp.repository.equipment;

import com.rikkei.salsp.entity.equipment.BorrowingRecord;
import com.rikkei.salsp.entity.equipment.BorrowingStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository quản lý truy vấn dữ liệu thực thể BorrowingRecord.
 */
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {
    /**
     * Tìm kiếm buổi cố vấn học thuật.
     * @param sessionId Tham số đầu vào sessionId

     * @return Kết quả trả về của phương thức
     */
    Optional<BorrowingRecord> findBySessionId(Long sessionId);

    /**
     * Tìm kiếm.
     * @param status Tham số đầu vào status

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
        SELECT br FROM BorrowingRecord br
        LEFT JOIN FETCH br.session s
        LEFT JOIN FETCH s.student st
        LEFT JOIN FETCH st.profile p
        WHERE br.id = :id
    """)
    Optional<BorrowingRecord> findByIdWithAssociations(@Param("id") Long id);

    /**
     * Đếm số lượng.
     * @param status Tham số đầu vào status

     * @return Kết quả trả về của phương thức
     */
    long countByStatus(BorrowingStatus status);

    @Query("SELECT COUNT(DISTINCT br.id) FROM BorrowingRecord br WHERE br.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<BorrowingStatus> statuses);
}
