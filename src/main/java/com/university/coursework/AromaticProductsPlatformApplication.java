package com.university.coursework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
@EnableAsync
public class AromaticProductsPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AromaticProductsPlatformApplication.class, args);
    }
}