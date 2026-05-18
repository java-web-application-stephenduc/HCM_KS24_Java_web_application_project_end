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

