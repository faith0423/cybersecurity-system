package com.example.cybersecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CybersecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(CybersecurityApplication.class, args);
	}

}
