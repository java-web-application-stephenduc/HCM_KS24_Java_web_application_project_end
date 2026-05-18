package com.rikkei.salsp.repository;

import com.rikkei.salsp.entity.AcademicEvaluation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicEvaluationRepository extends JpaRepository<AcademicEvaluation, Long> {
    boolean existsBySessionId(Long sessionId);

    Optional<AcademicEvaluation> findBySessionId(Long sessionId);
}

