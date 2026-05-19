package com.rikkei.salsp.repository.session;

import com.rikkei.salsp.entity.session.AcademicEvaluation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý truy vấn dữ liệu thực thể AcademicEvaluation.
 */
public interface AcademicEvaluationRepository extends JpaRepository<AcademicEvaluation, Long> {
    /**
     * Kiểm tra tồn tại của buổi cố vấn học thuật.
     * @param sessionId Tham số đầu vào sessionId

     * @return Kết quả trả về của phương thức
     */
    boolean existsBySessionId(Long sessionId);

    /**
     * Tìm kiếm buổi cố vấn học thuật.
     * @param sessionId Tham số đầu vào sessionId

     * @return Kết quả trả về của phương thức
     */
    Optional<AcademicEvaluation> findBySessionId(Long sessionId);
}

