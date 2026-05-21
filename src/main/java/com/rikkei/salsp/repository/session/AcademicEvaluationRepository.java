package com.rikkei.salsp.repository.session;

import com.rikkei.salsp.entity.session.AcademicEvaluation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cung cấp truy vấn cho thực thể AcademicEvaluation (điểm đánh giá buổi cố vấn).
 */
public interface AcademicEvaluationRepository extends JpaRepository<AcademicEvaluation, Long> {
    /* Kiểm tra buổi cố vấn đã được giảng viên đánh giá hay chưa (dùng để chống duplicate evaluation) */
    boolean existsBySessionId(Long sessionId);

    /* Truy xuất kết quả đánh giá (điểm + feedback) của một buổi cố vấn */
    Optional<AcademicEvaluation> findBySessionId(Long sessionId);
}

