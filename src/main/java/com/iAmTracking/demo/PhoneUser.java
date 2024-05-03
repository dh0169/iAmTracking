package com.iAmTracking.demo;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

public class PhoneUser implements UserDetails {
    private String phoneNum;
    private Map<LocalDateTime, ArrayList<Message>> conversations; //Conversation with ChatGPT

    public PhoneUser(String phoneNum, Map<LocalDateTime, ArrayList<Message>> conversations){
        this.phoneNum = phoneNum;
        this.conversations = conversations;
    }

    public PhoneUser(PhoneUser user){
        if(user != null) {
            this.phoneNum = user.getPhoneNum();
            this.conversations = user.conversations;
        }else{
            this.phoneNum = null;
            this.conversations = null;
        }
    }

    public PhoneUser(String phoneNum){
        //Empty conversation map
        this(phoneNum, new HashMap<LocalDateTime, ArrayList<Message>>());
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setConversations(Map<LocalDateTime, ArrayList<Message>> conversations) {
        this.conversations = conversations;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public Map<LocalDateTime, ArrayList<Message>> getConversations() {
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
}