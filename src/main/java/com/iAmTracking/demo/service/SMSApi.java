package com.iAmTracking.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.Message;
import org.springframework.http.ResponseEntity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SMSApi implements APIMessenger{

    private String smsApiUrl;
    private String smsApiKey;
    private ObjectMapper objectMapper;

    public SMSApi(String smsApiUrl, String smsApiKey, ObjectMapper objectMapper) {
        this.smsApiUrl = smsApiUrl;
        this.smsApiKey = smsApiKey;
        this.objectMapper = objectMapper;
    }

    public boolean sendSMS(String number, String body) {
        try {
            // http://smsApi.com/ + send
            String smsSendURL = this.smsApiUrl+"send";

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("API_KEY", this.smsApiKey);
            String requestBody = "{\"msg\":{\"number\":\""+ number +"\", \"body\":\""+body+"\"}}";

            ResponseEntity<String> connection = send(smsSendURL, requestBody, "POST", headers);

            int responseCode = connection.getStatusCode().value();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse the response body to check the message
                HashMap<String, String> responseMap = objectMapper.readValue(connection.getBody(), new TypeReference<HashMap<String, String>>() {});
                if ("msg sent...allegedly".equals(responseMap.get("msg"))) {
                    System.out.println("SMS sent successfully to " + number);
                    return true;
                } else {
                    System.out.println("SMS sending failed with message: " + responseMap.get("msg"));
                }
            } else {
                System.out.println("Failed to send SMS. HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Message> listSMS(String number) {
        try {
            URL url = new URL(smsApiUrl + "list");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("API_KEY", smsApiKey);
            connection.setDoOutput(true);

            String requestBody = "{\"number\":\"" + number + "\"}";
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

                return objectMapper.readValue(responseBody.toString(), new TypeReference<List<Message>>() {});
            } else {
                System.out.println("Failed to fetch messages. HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch messages: " + e.getMessage());
        }
        return null;
    }


}
