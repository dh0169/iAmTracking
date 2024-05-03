package com.iAmTracking.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SMSApi {

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
            URL url = new URL(smsApiUrl + "send");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("API_KEY", smsApiKey);
            connection.setDoOutput(true);

            String requestBody = "{\"msg\":{\"number\":\""+ number +"\", \"body\":\""+body+"\"}}";
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

                // Parse the response body to check the message
                Map<String, String> responseMap = objectMapper.readValue(responseBody.toString(), new TypeReference<Map<String, String>>() {});
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
