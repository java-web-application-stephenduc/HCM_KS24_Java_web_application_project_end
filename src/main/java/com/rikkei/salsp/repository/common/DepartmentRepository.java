package com.rikkei.salsp.repository.common;

import com.rikkei.salsp.entity.common.Department;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý truy vấn dữ liệu thực thể Department.
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    /**
     * Tìm kiếm.
     * @param code Tham số đầu vào code

     * @return Kết quả trả về của phương thức
     */
    Optional<Department> findByCode(String code);
}

