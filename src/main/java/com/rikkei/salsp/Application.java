package com.rikkei.salsp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Application entry point.
 *
 * MỤC ĐÍCH: Khởi động ứng dụng SALSP.
 *
 * ANNOTATIONS:
 * - @SpringBootApplication: Enable auto-config, component scanning, configuration
 * - @EnableScheduling: Enable @Scheduled tasks (ví dụ: BorrowingOverdueScheduler)
 *
 * STARTUP:
 * - Gradle: ./gradlew bootRun
 * - IDE: Run main() method
 * - JAR: java -jar SALSP-0.0.1-SNAPSHOT.jar
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    /**
     * Main entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
