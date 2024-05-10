package com.iAmTracking.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.Message;

<<<<<<< Updated upstream
=======
import com.iAmTracking.demo.config.WebSecurityConfig;
>>>>>>> Stashed changes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class GPTApi implements APIMessenger{
    private String gptApiUrl;
    private String gptApiKey;


    //Use this to parse json response to actual java object, Example below
    //Map<String, String> responseMap = objectMapper.readValue(responseBody.toString(), new TypeReference<Map<String, String>>() {});
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    public GPTApi(String gptApiUrl, String gptApiKey, ObjectMapper objectMapper) {
        this.gptApiUrl = gptApiUrl;
        this.gptApiKey = gptApiKey;
        this.objectMapper = objectMapper;
    }


    // Append all to a singular string, put that in place of message.getBody() in content part of body req
    public String summarize(List<Message> messages) throws JsonProcessingException {
        String gptReqString = "This is what I did today, please summarize it: ";
        for(int i = 0; i < messages.size(); i++) {
            Message currMessage = messages.get(i);
            if(currMessage.getType().equals("inbox")) {
                gptReqString = gptReqString + currMessage.getBody() + ", ";
            }
        }

        String summarizedResponse = sendChat(gptReqString);
        return summarizedResponse;
    }


<<<<<<< Updated upstream
    private String sendChat(String message) {
=======
    public String sendChat(String message) {
>>>>>>> Stashed changes
        // This method takes in a Message object and sends the body to the GPT API
        // This class implements APIMessenger, so use send() to send the actual web request

        // Example to guide implementation:
        // HashMap<String, String> headers = new HashMap<>();
        // headers.put("Content-Type", "application/json");
        // headers.put("API_KEY", "<api key needed in headers? put here>");
        // ResponseEntity<String> response = this.send(this.gptApiUrl, message.getBody(), "POST", headers);



        // Solution provided by GPT
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        // Set the model to gpt 3.5, message sent to the LLM is the body of the message object passed as a param
        // Use the prompt field to only send one request without a conversational context
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");

        // Need to create a list within messages that contains the role (user/system)
        // The "content" section is the prompt that feed directly to GPT
        List<Map<String, Object>> messagesList = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messagesList.add(userMessage);
        requestBody.put("messages", messagesList);

        // Wait for the response message, and return the specific output from GPT + error handling
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        try {
<<<<<<< Updated upstream
            ResponseEntity<String> response = restTemplate.postForEntity(gptApiUrl, entity, String.class);
=======
            ResponseEntity<String> response = new RestTemplate().postForEntity(gptApiUrl, entity, String.class);
>>>>>>> Stashed changes
            return extractGPTTextResponse(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("Error response from API: " + e.getResponseBodyAsString());
            throw new RuntimeException("API call to OpenAI failed: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("API call failed: " + e.getMessage());
            throw new RuntimeException("Error during API call to OpenAI: " + e.getMessage(), e);
        }
    }

    // Extract the response from GPT + error handling
    private String extractGPTTextResponse(String jsonResponse) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                // Assuming there's only one choice in the list
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null) {
                    String role = (String) message.get("role");
                    if ("assistant".equals(role)) {
                        return (String) message.get("content");
                    }
                }
            }
            return "No response from GPT";
        } catch (Exception e) {
            System.err.println("Failed to parse JSON response: " + e.getMessage());
            return "Failed to parse response from GPT";
        }
    }

<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes
