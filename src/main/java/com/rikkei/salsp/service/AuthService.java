package com.rikkei.salsp.service;

import com.rikkei.salsp.dto.RegisterDto;
import com.rikkei.salsp.entity.User;
import com.rikkei.salsp.entity.UserProfile;
import com.rikkei.salsp.entity.UserRole;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.DuplicateEmailException;
import com.rikkei.salsp.repository.UserProfileRepository;
import com.rikkei.salsp.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

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

