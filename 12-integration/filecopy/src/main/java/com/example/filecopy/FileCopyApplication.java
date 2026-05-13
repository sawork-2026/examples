package com.example.filecopy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
public class FileCopyApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileCopyApplication.class, args);
    }
}
