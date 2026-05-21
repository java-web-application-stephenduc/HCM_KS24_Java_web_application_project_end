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
 * Custom handler dùng sau khi login thành công.
 *
 * MỤC ĐÍCH: Redirect người dùng đến dashboard phù hợp với role của họ.
 *
 * FLOW:
 * User submit form login → Spring Security validate credentials
 * → Success → Call onAuthenticationSuccess()
 * → Extract role từ Authentication object
 * → Redirect dạo dashboard tương ứng:
 *    - ADMIN → /admin/dashboard
 *    - LECTURER → /lecturer/dashboard
 *    - STUDENT → /student/dashboard
 *
 * CẤU HÌNH: Được set ở SecurityConfig.java
 * .formLogin(form -> form.successHandler(customAuthSuccessHandler))
 */
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Xử lý sau khi authentication thành công.
     *
     * LOGIC:
     * 1. Lấy tập hợp roles từ Authentication object
     * 2. Kiểm tra role (ADMIN > LECTURER > STUDENT)
     * 3. Redirect tới dashboard:
     *    - /admin/dashboard (nếu là ADMIN)
     *    - /lecturer/dashboard (nếu là LECTURER)
     *    - /student/dashboard (default, nếu là STUDENT)
     * 4. Include context path (ví dụ: /salsp/admin/dashboard)
     *
     * @param request HttpServletRequest (chứa context path, v.v.)
     * @param response HttpServletResponse gửi redirect
     * @param authentication Authentication object chứa roles
     * @throws IOException khi sendRedirect thất bại
     * @throws ServletException servlet-level error
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

