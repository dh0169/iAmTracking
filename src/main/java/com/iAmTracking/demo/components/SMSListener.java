package com.iAmTracking.demo;

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

                for (Message msg : newMessages){
                    msg.setNumber(PhoneAuthFilter.obtainPhoneNumber(msg.getNumber()));
                }

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

        /*

        We got a giant list of message and I want to update each user with new messages.
         */
        String phoneNumber = PhoneAuthFilter.obtainPhoneNumber(messages.get(0).getNumber());
        PhoneUser user = phoneRepository.findByPhone(phoneNumber);
        ConcurrentHashMap<LocalDate, List<Message>> newMsgs = new ConcurrentHashMap<>();


        for (int i = 1; i < messages.size(); i++) {
            Message message = messages.get(i);
            System.out.println("\n\n"+message.getNumber()+"\n\n");


            if (user == null) {
                // If the user doesn't exist, create a new user.
                continue;
                //user = phoneRepository.createNewUser(phoneNumber);
            }

//            LocalDate currLocalDate = message.getReceived().toLocalDate();
//            if(newMsgs.get(currLocalDate) == null){
//                newMessageList = Collections.synchronizedList(new ArrayList<Message>());
//                newMsgs.put(currLocalDate, newMessageList);
//            }
//
//            newMsgs.get(currLocalDate).add(message);



            // Add the message to the user's conversation map. You might need to adjust the key.
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
                    System.out.println("Adding msg to phoneNumber :" + phoneNumber);
                    specificPhoneNumMsgs.add(msg);
                }
            }


            user.updateConversation(key, specificPhoneNumMsgs);

            this.phoneRepository.saveUser(user);

        }

        // Optionally, log the updated state of users for verification or debugging.
        System.out.println("Updated Users: " + phoneRepository);
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
