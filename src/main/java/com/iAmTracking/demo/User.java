package com.iAmTracking.demo;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class User{
    private String phoneNum;

    public User(String phoneNum){
        this.phoneNum = phoneNum;
    }

    User(User user){ this(user.getPhoneNum()); }

    public String getPhoneNum(){
        return this.phoneNum;
    }

}
