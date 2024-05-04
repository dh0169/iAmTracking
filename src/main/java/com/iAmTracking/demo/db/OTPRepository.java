package com.iAmTracking.demo.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.Message;
import com.iAmTracking.demo.OneTimePasscode;
import com.iAmTracking.demo.PhoneUser;
import com.iAmTracking.demo.auth.filters.PhoneAuthFilter;
import com.iAmTracking.demo.service.SMSApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.auth.filters.PhoneAuthFilter;
import com.iAmTracking.demo.db.PhoneRepository;
import com.iAmTracking.demo.service.SMSApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OTPRepository {

    private ConcurrentHashMap<String, OneTimePasscode> activeCodes;

    public OTPRepository(ConcurrentHashMap<String, OneTimePasscode> activeCodes){
        this.activeCodes = activeCodes;
    }

    public OTPRepository(){
        this(new ConcurrentHashMap<>());
    }

    public OneTimePasscode getCode(String phone){
        return this.activeCodes.get(phone);
    }

    public void setCode(String phone, OneTimePasscode otp){
        this.activeCodes.put(phone, otp);
    }

    public OneTimePasscode removeCode(String phone){
        return this.activeCodes.remove(phone);
    }



//    @Async
//    @Scheduled(fixedRate = 1000)  // every 5000 milliseconds (5 seconds)
//    public void checkForNewMessages() {
//        try {
//            for (String phoneNumber : activeCodes.keySet()){
//                OneTimePasscode currOTP = activeCodes.get(phoneNumber);
//                if (currOTP.isExpired()) {
//                    System.out.println("\n\nOTPRepository.java\nRemoving Expired OTP Code: " +currOTP.getCode());
//                    activeCodes.remove(phoneNumber);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Failed to remove OTP Code (OTPRepository.java): " + e.getMessage());
//        }
//    }

}
