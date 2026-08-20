package com.example.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableScheduling
public class AuditLogApplication {
    public static void main(String[] args) {
        String adminPass = System.getenv("ADMIN_PASSWORD");
        if (!StringUtils.hasText(adminPass)) {
            System.err.println("FATAL: ADMIN_PASSWORD environment variable is missing. Halting startup.");
            System.exit(1);
        }
        SpringApplication.run(AuditLogApplication.class, args);
    }
}