package com.dexmon.archival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataArchivalApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataArchivalApplication.class, args);
    }
}





