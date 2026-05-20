package com.rikkei.salsp.config;

import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.repository.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to check if currently authenticated user has been dynamically locked/deactivated.
 * If deactivated, logs them out and redirects to /auth/locked.
 */
@Component
@RequiredArgsConstructor
public class UserStatusInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String email = auth.getName();
            // Check current active status from Database
            java.util.Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!user.isActive()) {
                    // Log out user cleanly
                    new SecurityContextLogoutHandler().logout(request, response, auth);
                    response.sendRedirect(request.getContextPath() + "/auth/locked");
                    return false;
                }
            }
        }
        return true;
    }
}
