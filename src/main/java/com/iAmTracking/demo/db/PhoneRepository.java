package com.iAmTracking.demo.db;

import com.iAmTracking.demo.PhoneUser;
import com.iAmTracking.demo.User;

import java.util.HashMap;
import java.util.Map;

public class PhoneRepository {

    //DB Stuff here

    /*
     createNewUser(String number) -> create new PhoneUser in db and return user or Null on fail;
    findByPhone(String number) -> find PhoneUser in db and return new PhoneUser or Null on fail;
    saveUser(PhoneUser user) -> Save user to db and return the saved user or Null on fail;
    * */

    private Map<String, PhoneUser> users;

    public PhoneRepository(Map<String, PhoneUser> users) {
        this.users = users;
    }

    public PhoneRepository() {
        this(new HashMap<String, PhoneUser>());
    }

    public PhoneUser findByPhone(String phone){
        if (this.users.containsKey(phone)){
            return this.users.get(phone);
        }
        return null;
    }

    public PhoneUser createNewUser(String phone){
        PhoneUser tmp = new PhoneUser(phone);
        this.users.put(phone, tmp);

        return tmp;
    }

    public PhoneUser saveUser(PhoneUser user){
        this.users.put(user.getPhoneNum(), user);
        return user;
    }
}
