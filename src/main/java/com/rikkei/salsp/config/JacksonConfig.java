package com.rikkei.salsp.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    // Trong Jackson 3.0, DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS mặc định đã là false
    // và ObjectMapper là immutable sau khi khởi tạo, do đó không cần cấu hình thủ công.
}
