package com.iAmTracking.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.boot.SpringApplication;

@Controller
    @SpringBootApplication
    @EnableAsync
    @EnableScheduling
    @ComponentScan
    public class Main {


        public static void main(String[] args) {
            SpringApplication.run(iAmTracking.class, args);
        }




}
