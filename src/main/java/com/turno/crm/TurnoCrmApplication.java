package com.turno.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TurnoCrmApplication {
    public static void main(String[] args) {
        SpringApplication.run(TurnoCrmApplication.class, args);
    }
}
