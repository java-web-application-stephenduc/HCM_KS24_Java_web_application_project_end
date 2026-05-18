package com.rikkei.salsp.repository;

import com.rikkei.salsp.entity.BorrowingRecord;
import com.rikkei.salsp.entity.BorrowingStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {
    Optional<BorrowingRecord> findBySessionId(Long sessionId);

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
}
