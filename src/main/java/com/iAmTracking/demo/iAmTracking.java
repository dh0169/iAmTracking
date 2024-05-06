package com.iAmTracking.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iAmTracking.demo.Message;
import com.iAmTracking.demo.OneTimePasscode;
import com.iAmTracking.demo.PhoneUser;
import com.iAmTracking.demo.components.SMSListener;
import com.iAmTracking.demo.auth.filters.PhoneAuthFilter;
import com.iAmTracking.demo.db.OTPRepository;
import com.iAmTracking.demo.db.PhoneRepository;
import com.iAmTracking.demo.service.SMSApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


@Controller
@SpringBootApplication
public class iAmTracking {
    @Autowired
    private final SecurityContextRepository securityContextRepository;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot will configure this bean for you


    @Autowired
    PhoneRepository phoneRepository;

    @Autowired
    OTPRepository otpRepository;

    @Autowired
    SMSApi smsApi;

    private final Integer otpTTL = 5; //OTP Codes will last for 5 minutes then expire.

    public iAmTracking(SecurityContextRepository securityContextRepository, PhoneRepository phoneRepository, OTPRepository otpRepository, SMSApi smsApi, ObjectMapper objectMapper) {
        this.phoneRepository = phoneRepository;
        this.securityContextRepository = securityContextRepository;
        this.otpRepository = otpRepository;
        this.smsApi = smsApi;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String index(Model model){
        return "index";
    }

    @PostMapping("/")
    public String OTPRequest(Model model, @RequestParam("phone") String phone, Authentication authentication) throws Exception {
        phone = PhoneAuthFilter.obtainPhoneNumber(phone);
        OneTimePasscode otp = this.otpRepository.getCode(phone);


        if(authentication != null){
            return "redirect:/journalDashboard";

        }else{
            String err = "";
            if (otp != null && otp.isExpired()) {
                err = "OTP Expired\n";
                this.otpRepository.removeCode(phone);
                model.addAttribute("msg", err);

                return "/";
            }else if(otp != null){
                err = "OTP still active\n";
                model.addAttribute("msg", err);
                model.addAttribute("phone", phone);

                return "login";
            }

            String humanFriendlyOTP = OneTimePasscode.generateCode();
            StringBuilder passcode = new StringBuilder();
            for (String split : humanFriendlyOTP.split("-")) {
                passcode.append(split);
            }

            this.otpRepository.setCode(phone, new OneTimePasscode(passcode.toString(), this.otpTTL));
            otp = this.otpRepository.getCode(phone);


            // Construct request body for user sign up
            this.smsApi.sendSMS(phone, "iAmTracking OTP: " + humanFriendlyOTP);


            //return otp
            System.out.println("\n\nCreated new OTP Code\nPhone: "+ phone +"\nOTP: "+ otp.getCode() + "\n\n");

        }

        model.addAttribute("phone", phone);


        return "login";
    }


    @GetMapping("/journalDashboard")
    public String journalDashboard(Model model, Authentication auth) {
        // Pass phone number to the view model

        if (auth != null) {
            //This is where we pull all data from the db using the phone number
            //and add that data to the model just like below
            String phoneNumber = (String) auth.getPrincipal();
            model.addAttribute("phoneNumber", phoneNumber);
            PhoneUser phoneUser = this.phoneRepository.findByPhone(phoneNumber);

            if(phoneUser == null){
                phoneUser = phoneRepository.createNewUser(phoneNumber);
                smsApi.sendSMS(phoneUser.getPhoneNum(), "Welcome to iAmTracking! I am your friendly AI Powered assistant. Please text me anything you may need help with. Thanks!");
            }


            //Create a post mapping for /journalDashboard that receives date as input and returns conversation
            LocalDate now = LocalDate.now();
            model.addAttribute("messages", phoneUser.getConversations().get(now));
            model.addAttribute("date", now);

        }

        return "journalDashboard";
    }




    @RequestMapping("/timeline")
    public String timeline(Model model) {
        return "timeline";
    }


}