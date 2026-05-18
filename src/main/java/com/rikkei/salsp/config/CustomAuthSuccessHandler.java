package com.rikkei.salsp.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String role = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse("ROLE_STUDENT");

        String redirectUrl = switch (role) {
            case "ROLE_ADMIN" -> "/admin/dashboard";
            case "ROLE_LECTURER" -> "/lecturer/dashboard";
            default -> "/student/dashboard";
        };

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}

