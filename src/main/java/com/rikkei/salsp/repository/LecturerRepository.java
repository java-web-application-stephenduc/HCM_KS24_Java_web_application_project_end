package com.rikkei.salsp.repository;

import com.rikkei.salsp.entity.Lecturer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    Optional<Lecturer> findByUserId(Long userId);

    @Query("""
        SELECT l FROM Lecturer l
        JOIN FETCH l.user u
        JOIN FETCH u.profile p
        JOIN FETCH l.department d
        WHERE d.id = :departmentId
    """)
    List<Lecturer> findByDepartmentIdWithProfile(@Param("departmentId") Long departmentId);
}

