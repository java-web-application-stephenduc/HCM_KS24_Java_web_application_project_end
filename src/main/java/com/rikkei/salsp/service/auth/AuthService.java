package com.rikkei.salsp.service.auth;

import com.rikkei.salsp.dto.auth.RegisterDto;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserProfile;
import com.rikkei.salsp.entity.user.UserRole;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.DuplicateEmailException;
import com.rikkei.salsp.repository.user.UserProfileRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý nghiệp vụ đăng ký tài khoản mới và kiểm tra trùng lặp email.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Tìm kiếm.
     * @param email Tham số đầu vào email

     * @return Kết quả trả về của phương thức
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Phương thức xử lý nghiệp vụ register.
     * @param dto Tham số đầu vào dto
     */
    @Transactional
    public void register(RegisterDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email đã tồn tại");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu không khớp");
        }

        User user = new User();
        user.setEmail(dto.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setActive(true);
        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(dto.getFullName().trim());
        profile.setPhone(dto.getPhone());
        userProfileRepository.save(profile);
    }
}

