package com.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MainApplication {

    public static void main(String[] args) {
        // Enforce UTC to resolve PostgreSQL TimeZone mismatch
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(MainApplication.class, args);
    }
}
