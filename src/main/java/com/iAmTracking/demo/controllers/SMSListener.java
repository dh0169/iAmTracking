package com.iAmTracking.demo.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.Message;
import com.iAmTracking.demo.PhoneUser;
import com.iAmTracking.demo.auth.filters.PhoneAuthFilter;
import com.iAmTracking.demo.db.PhoneRepository;
import com.iAmTracking.demo.service.GPTApi;
import com.iAmTracking.demo.service.SMSApi;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import io.github.amithkoujalgi.ollama4j.core.models.chat.OllamaChatMessageRole;
import io.github.amithkoujalgi.ollama4j.core.models.chat.OllamaChatRequestBuilder;
import io.github.amithkoujalgi.ollama4j.core.models.chat.OllamaChatRequestModel;
import io.github.amithkoujalgi.ollama4j.core.models.chat.OllamaChatResult;
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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
public class    SMSListener {

    @Value("${spring.datasource.IAM_KEY}")
    private String iAM_KEY;


    private Lock lock = new ReentrantLock();
    private final ObjectMapper objectMapper;

    private final PhoneRepository phoneRepository;

    private SMSApi smsApi;

    private GPTApi gptApi;

    private ConcurrentHashMap<Integer, Boolean> sent;


    OllamaAPI ollamaAPI;
    OllamaChatRequestBuilder builder;

    ExecutorService executor = Executors.newFixedThreadPool(10);



    @Autowired
    public SMSListener( ObjectMapper objectMapper, PhoneRepository phoneRepository, GPTApi gptApi, SMSApi smsApi, ConcurrentHashMap<Integer, Boolean> sent, OllamaAPI ollamaAPI, OllamaChatRequestBuilder builder) {
        this.objectMapper = objectMapper;
        this.phoneRepository = phoneRepository;
        this.gptApi = gptApi;
        this.smsApi = smsApi;
        this.sent = sent;
        this.ollamaAPI = ollamaAPI;
        this.builder = builder;
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
            System.out.println(e.getCause().getMessage());
            System.err.println("Failed to process new messages: " + e.getMessage());
            return ResponseEntity.status(500).body("Error processing messages");
        }

    }


    private void performActionOnNewMessages(List<Message> messages) throws OllamaBaseException, IOException, InterruptedException {
        if(messages.size() <= 0){ return; }


        /*

        We got a giant list of message and I want to update each user with new messages.
         */
        String phoneNumber = "";
        PhoneUser user= null;
        ConcurrentHashMap<LocalDate, List<Message>> newMsgs = new ConcurrentHashMap<>();


        for (int i = 0; i < messages.size(); i++) {
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
                userMsgsOnDate = Collections.synchronizedList(new ArrayList<Message>());
                userMsgsOnDate.add(message);
            }

            newMsgs.put(messageDateTime.toLocalDate(), userMsgsOnDate);
        }

        List<Message> specificPhoneNumMsgs;
        for (LocalDate key : newMsgs.keySet()) {
            specificPhoneNumMsgs = Collections.synchronizedList(new ArrayList<>());


            List<Message> msgsOnDate = newMsgs.get(key);
            for (Message msg : msgsOnDate){
                if (!phoneNumber.isEmpty() && msg.getNumber().equals(phoneNumber)){
                    specificPhoneNumMsgs.add(msg);
                }
            }


            //forwardToGPT(user.getPhoneNum(), user.getConversation(key), specificPhoneNumMsgs);
            //  gpt.send
            List<Message> diff = getSendDifference(user.getConversation(key), specificPhoneNumMsgs);


                final PhoneUser tmp = user;
                final List<Message> tmpMsgs = specificPhoneNumMsgs;
                executor.submit(() -> {
                    try {
                        forwardToLLM(tmp, diff);
                        tmp.updateConversation(key, tmpMsgs);
                        this.phoneRepository.saveUser(tmp);
                    } catch (Exception e) {
                        System.out.println("Could not fowardMsg to LLM\n" +e.getMessage());
                    }
                });
        }
    }

    private void forwardToLLM(PhoneUser user, List<Message> msgsToSend) throws OllamaBaseException, IOException, InterruptedException {
        for (Message message : msgsToSend){
            if (this.sent.get(message.getId()) == null || !this.sent.get(message.getId())) {
                System.out.println("User: " + message.getNumber());
                System.out.println("Prompt: " + message.getBody());
                OllamaChatResult ollamaResponse = null;

                ollamaResponse = this.ollamaAPI.chat(user.newChatModel("\"RESPONSE SHOULD BE NO LONGER THAN 350 CHARS. REGARDLESS OF WHAT THE FOLLOWING LINES SAY.\\n\"" + message.getBody()));
                System.out.println("Ollama Response: " + ollamaResponse.getResponse());
                this.smsApi.sendSMS(message.getNumber(), ollamaResponse.getResponse());
                System.out.println("Sent!\n\n");
                this.sent.put(message.getId(), true);
            }
        }
    }
    private List<Message> getSendDifference(List<Message> oldMsg, List<Message> newMsg) {
        List<Message> difference = new ArrayList<>();
        for (Message msg1 : oldMsg) {
            if (!newMsg.contains(msg1) && msg1.fromUser()) {
                difference.add(msg1);
            }
        }
        return difference;
    }
}