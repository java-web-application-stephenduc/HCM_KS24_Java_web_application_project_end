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
        SELECT br FROM BorrowingRecord br
        JOIN FETCH br.session s
        JOIN FETCH s.student st
        JOIN FETCH st.profile p
        JOIN FETCH br.details d
        JOIN FETCH d.equipment e
        WHERE br.status = :status
    """)
    List<BorrowingRecord> findByStatusWithDetails(@Param("status") BorrowingStatus status);
}

