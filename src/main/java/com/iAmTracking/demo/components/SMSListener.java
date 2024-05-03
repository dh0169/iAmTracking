package com.iAmTracking.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.db.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

@Component
public class SMSListener {

    private final String SMS_API_URL = "https://052f-172-56-208-80.ngrok-free.app/sms/";
    private final String SMS_API_KEY = "K2heeixGWD0ofCw4Q219bkRjK3o3I294T3QmV0xEbj4hdiVLRj0oXkMqVEZ1ZAo=";

    private final ObjectMapper objectMapper;
    private List<Message> lastMessages = new ArrayList<>();

    private PhoneRepository phoneRepository;

    @Autowired
    private SMSApi smsApi;

    @Autowired
    public SMSListener( ObjectMapper objectMapper, PhoneRepository phoneRepository, SMSApi smsApi) {
        this.objectMapper = objectMapper;
        this.phoneRepository = phoneRepository;
        this.smsApi = smsApi;
    }

    @Async
    @Scheduled(fixedRate = 5000)  // every 5000 milliseconds (5 seconds)
    public void checkForNewMessages() {
        try {
            URL url = new URL(SMS_API_URL + "list");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("API_KEY", SMS_API_KEY);
            connection.setDoOutput(true);

            String requestBody = "";  // Construct request body if necessary
            connection.getOutputStream().write(requestBody.getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder responseBody = new StringBuilder();
                while (scanner.hasNextLine()) {
                    responseBody.append(scanner.nextLine());
                }
                scanner.close();
                connection.disconnect();

                List<Message> newMessages = objectMapper.readValue(responseBody.toString(), new TypeReference<List<Message>>() {});
                if (!isSameContent(lastMessages, newMessages)) {
                    performActionOnNewMessages(newMessages);
                    lastMessages = new ArrayList<>(newMessages);  // Update the lastMessages list
                }
            } else {
                System.out.println("Failed to fetch messages. HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch new messages: " + e.getMessage());
        }
    }



    private void performActionOnNewMessages(List<Message> messages) {
        for (Message message : messages) {
            // Only process messages of type "inbox"
            if ("inbox".equals(message.getType())) {
                System.out.println("Users: " + this.phoneRepository);
                // Extract phone number and update corresponding PhoneUser
                String phoneNumber = message.getNumber();
                PhoneUser user = phoneRepository.findByPhone(phoneNumber);
                if (user == null) {
                    user = phoneRepository.createNewUser(phoneNumber);
                }
                // Here you can update the PhoneUser object as needed
                phoneRepository.saveUser(user);
            }
        }
    }

    private boolean isSameContent(List<Message> oldMessages, List<Message> newMessages) {
        if (oldMessages.size() != newMessages.size()) {
            return false;
        }
        for (int i = 0; i < newMessages.size(); i++) {
            if (!oldMessages.get(i).getId().equals(newMessages.get(i).getId())) {
                return false;
            }
        }
        return true;
    }
}
