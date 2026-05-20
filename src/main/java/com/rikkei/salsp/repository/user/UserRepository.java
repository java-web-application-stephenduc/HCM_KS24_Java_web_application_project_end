package com.rikkei.salsp.repository.user;

import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserRole;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý truy vấn dữ liệu thực thể User.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    @EntityGraph(attributePaths = {"profile"})
    Page<User> findAll(Pageable pageable);

    /**
     * Tìm kiếm bằng email.
     * @param email Email của người dùng
     * @return User tương ứng nếu tìm thấy
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm kiếm và phân trang người dùng theo vai trò.
     * @param role Vai trò người dùng
     * @param pageable Phân trang
     * @return Trang người dùng tương ứng
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    long countByActiveTrue();

    long countByRole(UserRole role);
}
