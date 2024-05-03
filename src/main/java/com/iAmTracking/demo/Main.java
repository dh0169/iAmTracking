package com.iAmTracking.demo;

import com.iAmTracking.demo.db.PhoneRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.boot.SpringApplication;

import java.util.HashMap;
import java.util.Map;

    @Controller
    @SpringBootApplication
    @EnableAsync
    @EnableScheduling
    @ComponentScan({"com.iAmTracking.demo"})
    public class Main {


        public static void main(String[] args) {
            SpringApplication.run(com.iAmTracking.demo.iAmTracking.class, args);
        }



}
