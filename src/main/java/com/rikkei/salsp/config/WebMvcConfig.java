package com.rikkei.salsp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC config to register UserStatusInterceptor.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserStatusInterceptor userStatusInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userStatusInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/favicon.ico", 
                    "/uploads/**", 
                    "/auth/locked", 
                    "/auth/login", 
                    "/auth/logout",
                    "/error"
                );
    }
}
