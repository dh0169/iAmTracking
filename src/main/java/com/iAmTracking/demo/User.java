package com.iAmTracking.demo;

public class User {
    private String phoneNum;
    private String password;

    User(String phoneNum, String password){
        this.phoneNum = phoneNum;
        this.password = password;
    }

    public String getPhoneNum(){
        return this.phoneNum;
    }
}
