package com.rikkei.salsp.repository.user;

import com.rikkei.salsp.entity.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý truy vấn dữ liệu thực thể User.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Tìm kiếm.
     * @param email Tham số đầu vào email

     * @return Kết quả trả về của phương thức
     */
    Optional<User> findByEmail(String email);
}

