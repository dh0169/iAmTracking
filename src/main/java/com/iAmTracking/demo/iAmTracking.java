package com.iAmTracking.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iAmTracking.demo.auth.tokens.PhoneNumberAuthToken;
import com.iAmTracking.demo.db.OTPRepository;
import com.iAmTracking.demo.db.PhoneRepository;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.thymeleaf.util.ArrayUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;


@Controller
@SpringBootApplication
public class iAmTracking {
    @Autowired
    private final SecurityContextRepository securityContextRepository;

    @Autowired
    PhoneRepository phoneRepository;

    @Autowired
    OTPRepository otpRepository;

    @Autowired
    SMSApi smsApi;

    public iAmTracking(SecurityContextRepository securityContextRepository, PhoneRepository phoneRepository, OTPRepository otpRepository, SMSApi smsApi) {
        this.phoneRepository = phoneRepository;
        this.securityContextRepository = securityContextRepository;
        this.otpRepository = otpRepository;
        this.smsApi = smsApi;
    }

    @GetMapping("/")
    public String index(Model model){
        return "index";
    }

    @PostMapping("/")
    public String OTPRequest(Model model, @RequestParam("phone") String phone) throws Exception{
        if(!isNumeric(phone)){
            System.out.println("\n\n\n'phone' is not numeric: "+phone+"\n\n\n");
            return "redirect:/";
        }

        OneTimePasscode otp = this.otpRepository.getCode(phone);
        if(otp == null || otp.isExpired()){
            String err = "";
            if(otp != null && otp.isExpired()){
                err = "OTP Expired after 1 minute\n";
                this.otpRepository.removeCode(phone);
            }

            String humanFriendlyOTP = OneTimePasscode.generateCode();
            StringBuilder passcode = new StringBuilder();
            for(String split : humanFriendlyOTP.split("-")){
                passcode.append(split);
            }

            this.otpRepository.setCode(phone, new OneTimePasscode(passcode.toString(), 1));
            otp = this.otpRepository.getCode(phone);





            // Construct request body for user sign up
            this.smsApi.sendSMS(phone, "iAmTracking OTP: "+humanFriendlyOTP);


            //return otp
            System.out.println(this.otpRepository.getCode(phone));

        }

        model.addAttribute("phone", phone);


        return "login";
    }





    //Acts as login and register. If user exists then login process occurs normally using 2FA
    //If user doesn't exist, then create a new user in the backend
    //Process looks the same to end user regardless
//    @PostMapping("/login")
//    public String login(Model model, Authentication authentication) throws Exception{
//        System.out.println("\n\n"+"mmmhmm"+"\n\n");
//        //if phone
//
//        //Login Algorithm
//        //Generate 2FA code, send to user and show 2FA box on frontend
//        //User enters code, if code matches then success, if not then fail
//
//        //Once authenticated, check if user exists in our database
//        //If user doesn't exist, create new entry in db
//        //Redirect to journaldashboard
//
//        //ra.addFlashAttribute("phoneNumber", phone);
//
//         //redirct to index with 2FA flag to show
//        return "redirect:/journalDashboard";
//    }

    //    @PostMapping("/login")
//    public String login(Model model, @RequestParam("phone") String phone, MFAAuthentication authentication,
//                        HttpServletRequest request, HttpServletResponse response) throws Exception{
//        String phoneNumber = getPhone(authentication);
//        System.out.println(phone);
//        System.out.println(authentication);
//        if (phoneNumber.equals("8312060419")) {
//            this.successHandler.onAuthenticationSuccess(request, response, authentication.getFirst());
//        }
//        else {
//            this.failureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));
//        }
//
//        //if phone
//
//        //Login Algorithm
//        //Generate 2FA code, send to user and show 2FA box on frontend
//        //User enters code, if code matches then success, if not then fail
//
//        //Once authenticated, check if user exists in our database
//        //If user doesn't exist, create new entry in db
//        //Redirect to journaldashboard
//
//        //ra.addFlashAttribute("phoneNumber", phone);
//
//         //redirct to index with 2FA flag to show
//        return "redirect:/journalDashboard";
//    }

    @GetMapping("/journalDashboard")
    public String journalDashboard(Model model, Authentication auth) {
        // Pass phone number to the view model
        LocalDateTime timePut = null;

        if (auth != null) {
            //This is where we pull all data from the db using the phone number
            //and add that data to the model just like below
            String phoneNumber = (String) auth.getPrincipal();
            model.addAttribute("phoneNumber", phoneNumber);
            PhoneUser phoneUser = this.phoneRepository.findByPhone(phoneNumber);
            if(phoneUser == null){
                HashMap<LocalDateTime, ArrayList<Message>> convo = new HashMap<>();
                ArrayList<Message> msgList = new ArrayList<>();
                msgList.add(new Message("8312060419", "Howdy partner"));

                LocalDateTime now = LocalDateTime.now();
                timePut = now;
                convo.put(now, msgList);
                phoneUser = new PhoneUser(phoneNumber, convo);
                this.phoneRepository.saveUser(phoneUser);
            }

            ObjectMapper mapper = new ObjectMapper();
            String convoJson;
            try{
                convoJson = mapper.writeValueAsString(phoneUser.getConversations());

            }catch (JsonProcessingException jsonErr){
                System.out.println(jsonErr);
                convoJson = "Empty conversation";
            }

            if (timePut != null){
                ArrayList<Message> msgs = phoneUser.getConversations().get(timePut);
                if (msgs != null) {
                    System.out.println("\n\nPhoneUser: " + msgs.get(0).getBody() + "\n\n");
                    model.addAttribute("journalEntry", msgs.get(0));
                }
            }

        }

        return "journalDashboard";
    }


//    @PostMapping("/retrieveJournalEntry")
//    public String retrieveJournalEntry(@RequestParam("entryDate") LocalDate entryDate,
//                                       @RequestParam("phoneNumber") String phoneNumber) {
//        System.out.println("Entry Date: " + entryDate);
//        System.out.println("Phone Number: " + phoneNumber);
//
//        return "redirect:/journalDashboard"; // Redirect to the journal dashboard page
//    }




    public static boolean isNumeric(String input) {
        return input.matches("^[0-9]+$");
    }

    @RequestMapping("/timeline")
    public String timeline(Model model) {
        return "timeline";
    }


}