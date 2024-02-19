package com.iAmTracking.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class DemoApplication {

	//Entry point for Spring Boot app
	public static void main(String[] args) {
		SpringApplication.run(iAmTracking.class, args);
	}
}
