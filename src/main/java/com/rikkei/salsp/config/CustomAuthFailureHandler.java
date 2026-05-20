package com.rikkei.salsp.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Custom authentication failure handler to redirect locked/disabled users to /auth/locked.
 */
@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof DisabledException || exception.getCause() instanceof DisabledException) {
            response.sendRedirect(request.getContextPath() + "/auth/locked");
        } else {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=true");
        }
    }
}
