package com.iAmTracking.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public interface APIMessenger {


    public default ResponseEntity<String> send(String apiUrl, String requestBody, String requestMethod, HashMap<String, String> headers){
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //Sets request method, GET, POST, PUT, etc. Loop sets headers
            connection.setRequestMethod(requestMethod);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            connection.setDoOutput(true);
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

                return ResponseEntity.ok(requestBody);

            } else {
                System.out.println("HTTP error code: " + responseCode);
                return ResponseEntity.status(responseCode).body("");
            }

        } catch (Exception e) {
            System.err.println("Error occured when sending request to api: " + e.getMessage());
            return ResponseEntity.internalServerError().body("");
        }


    }

}

