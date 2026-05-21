package com.rikkei.salsp.config;

import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Jackson ObjectMapper (JSON serialization).
 *
 * MỤC ĐÍCH: Tùy chỉnh cách serialize/deserialize JSON (nếu cần).
 *
 * CONTEXT:
 * - Jackson là default JSON provider của Spring Boot
 * - Serialize Java objects → JSON (REST API response)
 * - Deserialize JSON → Java objects (POST/PUT body)
 *
 * SPRING BOOT 3 CHANGES:
 * - DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS: mặc định false (write ISO-8601 format)
 * - Không cần manual config như Spring Boot 2
 * - ObjectMapper là immutable sau khởi tạo (best practice)
 *
 * CÓ THỂ CUSTOMIZE NẾU CẦN:
 *   @Bean
 *   public ObjectMapper objectMapper() {
 *       return new ObjectMapper()
 *           .registerModule(new JavaTimeModule())
 *           .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
 *           .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
 *   }
 */
@Configuration
public class JacksonConfig {
    // Mặc định không cần config, Spring Boot 4.x tự xử lý tốt
}
