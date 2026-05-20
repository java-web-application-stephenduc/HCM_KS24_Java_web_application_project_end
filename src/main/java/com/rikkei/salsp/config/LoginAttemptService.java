package com.rikkei.salsp.config;

import com.rikkei.salsp.repository.user.UserRepository;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    public void loginFailed(String email) {
        int attempts = attemptsCache.getOrDefault(email, 0) + 1;
        attemptsCache.put(email, attempts);
        log.warn("Login failed for {} (attempt {}/{})", email, attempts, MAX_ATTEMPTS);
        if (attempts >= MAX_ATTEMPTS) {
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setActive(false);
                userRepository.save(user);
                log.warn("Account locked for {} due to {} failed attempts", email, attempts);
            });
            attemptsCache.remove(email);
        }
    }

    public void loginSucceeded(String email) {
        attemptsCache.remove(email);
    }
}
