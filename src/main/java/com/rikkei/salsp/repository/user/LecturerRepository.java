package com.rikkei.salsp.repository.user;

import com.rikkei.salsp.entity.user.Lecturer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository cung cấp các truy vấn mở rộng cho thực thể Lecturer,
 * bao gồm JOIN FETCH nhiều tầng để tránh N+1 khi truy cập quan hệ.
 */
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    /* Truy vấn giảng viên theo ID tài khoản (dùng để kiểm tra giảng viên tồn tại) */
    Optional<Lecturer> findByUserId(Long userId);

    /*
     * Truy vấn giảng viên kèm thông tin khoa (department) qua JOIN FETCH.
     * join fetch giúp nạp department cùng lúc với Lecturer, tránh N+1
     * khi gọi l.getDepartment() ở service layer.
     */
    @Query("""
        SELECT l FROM Lecturer l
        JOIN FETCH l.department d
        WHERE l.user.id = :userId
    """)
    Optional<Lecturer> findByUserIdWithDepartment(@Param("userId") Long userId);

    /*
     * Truy vấn danh sách giảng viên thuộc một khoa, kèm thông tin tài khoản
     * (User) và hồ sơ (UserProfile) qua JOIN FETCH 3 tầng (lecturer → user → profile).
     * Giải pháp này triệt để tránh N+1 khi render tên giảng viên trên giao diện.
     */
    @Query("""
        SELECT l FROM Lecturer l
        JOIN FETCH l.user u
        JOIN FETCH u.profile p
        JOIN FETCH l.department d
        WHERE d.id = :departmentId
    """)
    List<Lecturer> findByDepartmentIdWithProfile(@Param("departmentId") Long departmentId);
}

