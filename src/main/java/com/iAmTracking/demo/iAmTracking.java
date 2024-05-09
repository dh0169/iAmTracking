package com.iAmTracking.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iAmTracking.demo.auth.filters.PhoneAuthFilter;
import com.iAmTracking.demo.db.OTPRepository;
import com.iAmTracking.demo.db.PhoneRepository;
import com.iAmTracking.demo.service.GPTApi;
import com.iAmTracking.demo.service.SMSApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpServerErrorException;

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
@SpringBootApplication
public class iAmTracking {
    @Autowired
    private final SecurityContextRepository securityContextRepository;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot will configure this bean for you



    //The main user database. Use this get user objects using a phone number
    @Autowired
    PhoneRepository phoneRepository;


    //Acts as the OTP database.
    @Autowired
    OTPRepository otpRepository;


    //Use this to interact with smsAPI.
    //smsAPI should have a sendSMS(String number, String msg) that returns true on success.
    @Autowired
    SMSApi smsApi;


    //Use this to interact with gptAPI.
    //gptAPI should have a sendChat(String msg) that returns the gpt response
    @Autowired
    GPTApi gptApi;


    private final Integer otpTTL = 5; //OTP Codes will last for 5 minutes then expire.

    public iAmTracking(SecurityContextRepository securityContextRepository, PhoneRepository phoneRepository, OTPRepository otpRepository, SMSApi smsApi, GPTApi gptApi, ObjectMapper objectMapper) {
        this.phoneRepository = phoneRepository;
        this.securityContextRepository = securityContextRepository;
        this.otpRepository = otpRepository;
        this.smsApi = smsApi;
        this.gptApi = gptApi;
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

        //This is where we pull all data from the db using the phone number
        //and add that data to the model just like below
        String phoneNumber = (String) auth.getPrincipal();
        model.addAttribute("phoneNumber", phoneNumber);
        PhoneUser phoneUser = this.phoneRepository.findByPhone(phoneNumber);

        //User doesn't exist, create user. Automatic registration on auth
        if(phoneUser == null){
            phoneUser = phoneRepository.createNewUser(phoneNumber);
            smsApi.sendSMS(phoneUser.getPhoneNum(), "Welcome to iAmTracking! I am your friendly AI Powered assistant. Please let me know how I can help\uD83D\uDE01");
        }


        LocalDate now = LocalDate.now();
        model.addAttribute("messages", phoneUser.getConversations().get(now));
        model.addAttribute("date", now);



        return "journalDashboard";
    }

    @PostMapping(value="/journalDashboard", consumes="application/json")
    public ResponseEntity<String> getJournalDates(Authentication auth, @RequestBody Map<String, String> body) {
        String phoneNumber = (String) auth.getPrincipal();
        PhoneUser phoneUser = this.phoneRepository.findByPhone(phoneNumber);

        try {
            LocalDate date = LocalDate.parse(body.get("date"));
            String json = "[]";

            if(phoneUser != null)
                json = objectMapper.writeValueAsString(phoneUser.getConversation(date));

            return new ResponseEntity<String>(json, HttpStatus.OK);
        } catch (DateTimeParseException parseException) {
            return new ResponseEntity<String>("{ \"msg\" : \"invalid date format, should be yyyy-MM-dd\"}", HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            System.out.println("\n\n\n"+e);
            return new ResponseEntity<String>("{Error occurred, please check request and try again}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @RequestMapping("/timeline")
    public String timeline(Model model) {
        return "timeline";
    }


}