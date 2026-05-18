package com.rikkei.salsp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SecuritySmokeTest {

    @Test
    void passwordEncoderMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("Test@1234");
        assertTrue(encoder.matches("Test@1234", hash));
    }
}

