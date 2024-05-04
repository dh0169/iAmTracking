package com.iAmTracking.demo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.synchronizedList;

public class PhoneUser implements UserDetails {
    ObjectMapper mapper = new ObjectMapper();

    private String phoneNum;

    private ConcurrentHashMap<LocalDate, List<Message>> conversations; //Conversation with ChatGPT

    public PhoneUser(String phoneNum, ConcurrentHashMap<LocalDate, List<Message>> conversations){
        this.phoneNum = phoneNum;
        this.conversations = conversations;
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());  // Register the JavaTimeModule
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional: disable writing dates as timestamps
    }

    public PhoneUser(PhoneUser user){
        if(user != null) {
            this.phoneNum = user.getPhoneNum();
            this.conversations = user.conversations;
        }else{
            this.phoneNum = null;
            this.conversations = null;
        }

        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());  // Register the JavaTimeModule
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional: disable writing dates as timestamps
    }

    public PhoneUser(String phoneNum){
        //Empty conversation map
        List<Message> tmpList = Collections.synchronizedList(new ArrayList<Message>());
        ConcurrentHashMap<LocalDate, List<Message>> tmpMap = new ConcurrentHashMap<>();
        this.phoneNum = phoneNum;
        this.conversations = tmpMap;
        this.conversations.put(LocalDate.now(), tmpList);
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());  // Register the JavaTimeModule
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional: disable writing dates as timestamps
    }

    // Method to add a message to a conversation
    public void addMessageToConversation(LocalDate date, Message message) {
        if(this.conversations.containsKey(date)){
            this.conversations.get(date).add(message);
            return;
        }
        this.conversations.computeIfAbsent(date, k -> new ArrayList<>()).add(message);
    }

    public void updateConversation(LocalDate date, List<Message> messages) {
        this.conversations.put(date, messages);
    }


    public ArrayList<Message> getConversation(LocalDate date) {
        if (conversations.containsKey(date)) {
            ArrayList<Message> messages = new ArrayList<>(conversations.get(date));
            Collections.sort(messages);
            return messages;
        }
        return new ArrayList<>(); // Return an empty array if no conversation exists for the given date
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setConversations(ConcurrentHashMap<LocalDate, List<Message>> conversations) {
        this.conversations = conversations;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public Map<LocalDate, List<Message>> getConversations() {
        return conversations;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return getPhoneNum();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Error converting PhoneUser to JSON: " + e.getMessage();
        }
    }
}