package com.rikkei.salsp.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Lớp `CustomAuthSuccessHandler` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Phương thức xử lý nghiệp vụ onAuthenticationSuccess.
     * @param request Tham số đầu vào request
     * @param response Tham số đầu vào response
     * @param authentication Tham số đầu vào authentication
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        java.util.Set<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(java.util.stream.Collectors.toSet());

        String redirectUrl;
        if (roles.contains("ROLE_ADMIN")) {
            redirectUrl = "/admin/dashboard";
        } else if (roles.contains("ROLE_LECTURER")) {
            redirectUrl = "/lecturer/dashboard";
        } else {
            redirectUrl = "/student/dashboard";
        }

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}

