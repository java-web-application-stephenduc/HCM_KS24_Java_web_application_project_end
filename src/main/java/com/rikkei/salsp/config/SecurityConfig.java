package com.rikkei.salsp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình Spring Security cho toàn bộ ứng dụng SALSP.
 *
 * MỤC ĐÍCH:
 * - Định nghĩa authorization rules (ai được phép vào endpoint nào)
 * - Cấu hình form login / logout
 * - Cấu hình password encoding
 * - Cho phép tài nguyên tĩnh bypass Security Filter Chain
 *
 * AUTHORIZATION RULES:
 * - /auth/**: PUBLIC (không cần đăng nhập)
 * - /admin/**: ADMIN role (Admin)
 * - /lecturer/**: LECTURER role (Giảng viên)
 * - /student/**: STUDENT role (Sinh viên)
 * - /profile/**, /dashboard: AUTHENTICATED (bất kỳ vai trò nào đã login)
 * - Còn lại: AUTHENTICATED (cần đăng nhập)
 *
 * SPRING SECURITY 6+ CHANGES:
 * - Sử dụng SecurityFilterChain (thay vì WebSecurityConfigurerAdapter deprecated)
 * - Sử dụng requestMatchers() (thay vì antMatchers())
 * - CSRF bật mặc định trên POST/PUT/DELETE
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthSuccessHandler customAuthSuccessHandler;
    private final CustomAuthFailureHandler customAuthFailureHandler;

    /**
     * Cấu hình HTTP Security - Authorization Rules & Login/Logout.
     *
     * FLOW:
     * 1. authorizeHttpRequests: Định nghĩa ai được vào endpoint nào
     * 2. formLogin: Cấu hình form login (page, URL, success/failure handler)
     * 3. logout: Cấu hình logout URL & redirect sau logout
     * 4. exceptionHandling: Xử lý access denied (403 Forbidden)
     *
     * AUTHORIZATION DETAILS:
     * - requestMatchers("/auth/**", "/error").permitAll(): PUBLIC - ai cũng vào được
     * - requestMatchers("/admin/**").hasRole("ADMIN"): Chỉ Admin vào được
     * - requestMatchers("/lecturer/**").hasRole("LECTURER"): Chỉ Giảng viên vào được
     * - requestMatchers("/student/**").hasRole("STUDENT"): Chỉ Sinh viên vào được
     * - requestMatchers("/profile/**", "/dashboard").authenticated(): Ai đã LOGIN được vào
     * - anyRequest().authenticated(): Endpoint khác cần đăng nhập
     *
     * LOGIN CONFIGURATION:
     * - loginPage("/auth/login"): Form login ở đây
     * - loginProcessingUrl("/auth/login"): Submit form form login tới URL này
     * - successHandler: Gọi CustomAuthSuccessHandler khi login thành công
     *   (redirect tới dashboard tương ứng role)
     * - failureHandler: Gọi CustomAuthFailureHandler khi login fail
     *   (xử lý LoginAttemptService, account locked, v.v.)
     *
     * LOGOUT CONFIGURATION:
     * - logoutUrl("/auth/logout"): POST /auth/logout để logout
     * - logoutSuccessUrl("/auth/login?logout=true"): Redirect tới login với flag logout
     *   Template dùng flag này để hiển thị "Logged out" message
     *
     * EXCEPTION HANDLING:
     * - accessDeniedPage("/error/403"): Khi user login nhưng không đủ quyền
     *   -> Redirect tới 403 page
     *
     * @param http HttpSecurity - builder pattern để cấu hình security
     * @return SecurityFilterChain - filter chain được build
     * @throws Exception từ HttpSecurity.build()
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // === AUTHORIZATION RULES ===
            .authorizeHttpRequests(auth -> auth
                // PUBLIC endpoints - ai cũng vào được (không cần login)
                .requestMatchers("/auth/**", "/error").permitAll()

                // ADMIN-only endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // LECTURER-only endpoints
                .requestMatchers("/lecturer/**").hasRole("LECTURER")

                // STUDENT-only endpoints
                .requestMatchers("/student/**").hasRole("STUDENT")

                // Shared endpoints - ai đã login cũng được (bất kỳ role)
                .requestMatchers("/profile/**", "/dashboard").authenticated()

                // Endpoint khác: cần đăng nhập
                .anyRequest().authenticated()
            )
            // === FORM LOGIN CONFIGURATION ===
            .formLogin(form -> form
                // Hiển thị form login ở GET /auth/login
                .loginPage("/auth/login")

                // Submit form login tới POST /auth/login (Spring Security xử lý)
                // Spring Security sẽ:
                // 1. Lấy email + password từ request parameter (mặc định: username, password)
                // 2. Load User via CustomUserDetailsService
                // 3. Kiểm tra password (BCrypt compare)
                // 4. So sánh authority (role)
                .loginProcessingUrl("/auth/login")

                // Login thành công: gọi CustomAuthSuccessHandler
                // -> Redirect tới dashboard của role tương ứng
                .successHandler(customAuthSuccessHandler)

                // Login thất bại: gọi CustomAuthFailureHandler
                // -> Xử lý account locked, invalid credentials, v.v.
                .failureHandler(customAuthFailureHandler)

                // Form login page được public (không cần login để vào)
                .permitAll()
            )
            // === LOGOUT CONFIGURATION ===
            .logout(logout -> logout
                // POST /auth/logout để logout
                .logoutUrl("/auth/logout")

                // Sau logout, redirect tới login page với flag logout=true
                // Template kiểm tra flag này để hiển thị "Logged out successfully" message
                .logoutSuccessUrl("/auth/login?logout=true")

                // Logout link cũng được public
                .permitAll()
            )
            // === EXCEPTION HANDLING ===
            .exceptionHandling(ex -> ex
                // Khi user login nhưng không đủ quyền throw AccessDeniedException
                // -> Redirect tới 403 Forbidden page
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }

    /**
     * Cấu hình WebSecurityCustomizer - cho phép tài nguyên tĩnh bypass Security Filter Chain.
     *
     * MỤC ĐÍCH:
     * - Tài nguyên tĩnh (CSS, JS, images) không cần login để truy cập
     * - Tránh overhead của Filter Chain cho các file static
     *
     * RESOURCES:
     * - /css/**: Tất cả CSS file
     * - /js/**: Tất cả JavaScript file
     * - /images/**: Tất cả image file
     * - /favicon.ico: Favicon
     * - /uploads/**: Thư mục upload (avatar, doc, v.v.)
     *
     * SPRING SECURITY 6+ SYNTAX:
     * - web.ignoring().requestMatchers(...): Loại bỏ các pattern khỏi Security Filter Chain
     * - Nếu không, tài nguyên tĩnh sẽ phải qua Spring Security filter (lãng phí CPU/Memory)
     *
     * @return WebSecurityCustomizer bean
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/uploads/**");
    }

    /**
     * Cấu hình Password Encoder - Mã hóa mật khẩu BCrypt.
     *
     * MỤC ĐÍCH:
     * - Spring Security sử dụng PasswordEncoder này khi:
     *   1. Encode password khi user đăng ký (ở AuthService)
     *   2. So sánh password khi login (ở DaoAuthenticationProvider)
     *
     * BCrypt:
     * - Adaptive hash function: Mỗi lần encode cùng password sẽ khác nhau (có salt ngẫu nhiên)
     * - Vòng tính toán (strength): Mặc định 10 (càng cao càng bảo mật, càng chậm)
     * - Trên server có thể tăng strength theo thời gian khi CPU mạnh hơn
     *
     * BẢO MẬT:
     * - NEVER lưu plaintext password
     * - NEVER decode BCrypt (nó one-way hashing)
     * - Để verify password: passwordEncoder.matches(rawPassword, hashedPassword)
     *
     * @return BCryptPasswordEncoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

