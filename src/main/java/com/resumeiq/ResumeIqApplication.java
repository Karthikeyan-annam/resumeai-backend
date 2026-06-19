package com.resumeiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application entry point for the ResumeIQ AI SaaS Platform.
 */
@SpringBootApplication
@EnableAsync
public class ResumeIqApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeIqApplication.class, args);
    }
}
