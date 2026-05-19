package com.rikkei.salsp.repository.user;

import com.rikkei.salsp.entity.user.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý truy vấn dữ liệu thực thể UserProfile.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    /**
     * Tìm kiếm người dùng.
     * @param userId Tham số đầu vào userId

     * @return Kết quả trả về của phương thức
     */
    Optional<UserProfile> findByUserId(Long userId);
}

