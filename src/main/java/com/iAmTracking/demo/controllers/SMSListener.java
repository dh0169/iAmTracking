package com.iAmTracking.demo.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.Message;
import com.iAmTracking.demo.PhoneUser;
import com.iAmTracking.demo.auth.filters.PhoneAuthFilter;
import com.iAmTracking.demo.db.PhoneRepository;
import com.iAmTracking.demo.service.GPTApi;
import com.iAmTracking.demo.service.SMSApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SMSListener {

    @Value("${spring.datasource.IAM_KEY}")
    private String iAM_KEY;


    private final ObjectMapper objectMapper;

    private PhoneRepository phoneRepository;

    private GPTApi gptApi;

    @Autowired
    public SMSListener( ObjectMapper objectMapper, PhoneRepository phoneRepository, GPTApi gptApi) {
        this.objectMapper = objectMapper;
        this.phoneRepository = phoneRepository;
        this.gptApi = gptApi;
    }



    @PostMapping(value="/lsaGqZsUS9ttzw2qMtx6znYYu36Kr4", consumes ="application/json")
    public ResponseEntity<String> receiveMessages(@RequestBody List<Message> newMessages, @RequestHeader("IAM_KEY") String apiKey) {
        if (iAM_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API Key");
        }

        try {
            for (Message msg : newMessages) {
                msg.setNumber(PhoneAuthFilter.obtainPhoneNumber(msg.getNumber()));
            }

            performActionOnNewMessages(newMessages);


            return ResponseEntity.ok("Messages processed successfully.");
        } catch (Exception e) {
            System.err.println("Failed to process new messages: " + e.getMessage());
            return ResponseEntity.status(500).body("Error processing messages");

        }
    }


    private void performActionOnNewMessages(List<Message> messages) {
        if(messages.size() <= 0){ return; }


        /*

        We got a giant list of message and I want to update each user with new messages.
         */
        String phoneNumber = PhoneAuthFilter.obtainPhoneNumber(messages.get(0).getNumber());
        PhoneUser user = phoneRepository.findByPhone(phoneNumber);
        ConcurrentHashMap<LocalDate, List<Message>> newMsgs = new ConcurrentHashMap<>();


        for (int i = 1; i < messages.size(); i++) {
            phoneNumber = PhoneAuthFilter.obtainPhoneNumber(messages.get(i).getNumber());
            user = phoneRepository.findByPhone(phoneNumber);

            Message message = messages.get(i);

            if (user == null) {
                // If the user doesn't exist, ignore the message(We can see messages).
                continue;
            }


            // Here, I use the message's received timestamp. Adjust according to your application's needs.
            LocalDateTime messageDateTime = message.getReceived(); // Ensure this is parsed or set correctly in your message object.
            List<Message> userMsgsOnDate = newMsgs.get(messageDateTime.toLocalDate());
            if( userMsgsOnDate != null && !userMsgsOnDate.contains(message)){
                userMsgsOnDate.add(message);
            }else if(userMsgsOnDate == null){
                userMsgsOnDate = new ArrayList<Message>();
                userMsgsOnDate.add(message);
            }

            newMsgs.put(messageDateTime.toLocalDate(), userMsgsOnDate);


            //for each localdate key in the newMsg, create new list and add all with same thread id. update userMsgsOnDate.updateConversation.



            // Save the updated user in the repository.
        }
        List<Message> specificPhoneNumMsgs;
        for (LocalDate key : newMsgs.keySet()) {
            specificPhoneNumMsgs = Collections.synchronizedList(new ArrayList<>());


            List<Message> msgsOnDate = newMsgs.get(key);
            for (Message msg : msgsOnDate){
                if (msg.getNumber().equals(phoneNumber)){
                    specificPhoneNumMsgs.add(msg);
                }
            }

            //message here depending on List[Message] difference.
            //if(shouldForwardToGpt(user, specificPhoneNumMsgs))
            //  gpt.send
            user.updateConversation(key, specificPhoneNumMsgs);


            this.phoneRepository.saveUser(user);

        }

        // Optionally, log the updated state of users for verification or debugging.
        System.out.println("Updated Users: " + phoneRepository);
    }

}
