package com.iAmTracking.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.Message;

import java.util.HashMap;
import java.util.Map;

public class GPTApi implements APIMessenger{
    private String gptApiUrl;
    private String gptApiKey;

    //Use this to parse json response to actual java object, Example below
    //Map<String, String> responseMap = objectMapper.readValue(responseBody.toString(), new TypeReference<Map<String, String>>() {});
    private ObjectMapper objectMapper;

    public GPTApi(String gptApiUrl, String gptApiKey, ObjectMapper objectMapper) {
        this.gptApiUrl = gptApiUrl;
        this.gptApiKey = gptApiKey;
        this.objectMapper = objectMapper;
    }

    public String sendChat(Message message) {
        // This method takes in a Message object and sends the body to the GPT API
        // This class implements APIMessenger, so use send() to send the actual web request

        // Example to guide implementation:
        // HashMap<String, String> headers = new HashMap<>();
        // headers.put("Content-Type", "application/json");
        // headers.put("API_KEY", "<api key needed in headers? put here>");
        // ResponseEntity<String> response = this.send(this.gptApiUrl, message.getBody(), "POST", headers);


        return "return gpt response here";
    }
}
