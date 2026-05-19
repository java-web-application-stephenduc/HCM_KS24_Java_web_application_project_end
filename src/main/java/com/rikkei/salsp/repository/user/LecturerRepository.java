package com.rikkei.salsp.repository.user;

import com.rikkei.salsp.entity.user.Lecturer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository quản lý truy vấn dữ liệu thực thể Lecturer.
 */
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    /**
     * Tìm kiếm người dùng.
     * @param userId Tham số đầu vào userId

     * @return Kết quả trả về của phương thức
     */
    Optional<Lecturer> findByUserId(Long userId);

    @Query("""
        SELECT l FROM Lecturer l
        JOIN FETCH l.department d
        WHERE l.user.id = :userId
    """)
    Optional<Lecturer> findByUserIdWithDepartment(@Param("userId") Long userId);

    @Query("""
        SELECT l FROM Lecturer l
        JOIN FETCH l.user u
        JOIN FETCH u.profile p
        JOIN FETCH l.department d
        WHERE d.id = :departmentId
    """)
    List<Lecturer> findByDepartmentIdWithProfile(@Param("departmentId") Long departmentId);
}

