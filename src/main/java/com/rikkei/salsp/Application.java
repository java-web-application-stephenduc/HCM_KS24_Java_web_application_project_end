package com.rikkei.salsp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Lớp `Application` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    public /**
  * Phương thức xử lý nghiệp vụ main.
  * @param args Tham số đầu vào args
  */
 static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
