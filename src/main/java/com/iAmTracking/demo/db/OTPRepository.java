package com.iAmTracking.demo.db;

import com.iAmTracking.demo.OneTimePasscode;

import java.util.HashMap;
import java.util.Map;

public class OTPRepository {
    private Map<String, OneTimePasscode> activeCodes;

    public OTPRepository(Map<String, OneTimePasscode> activeCodes){
        this.activeCodes = activeCodes;
    }

    public OTPRepository(){
        this(new HashMap<String, OneTimePasscode>());
    }

    public OneTimePasscode getCode(String phone){
        return this.activeCodes.get(phone);
    }

    public void setCode(String phone, OneTimePasscode otp){
        this.activeCodes.put(phone, otp);
    }

    public OneTimePasscode removeCode(String phone){
        return this.activeCodes.remove(phone);
    }



}
