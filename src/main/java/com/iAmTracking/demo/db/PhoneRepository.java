package com.iAmTracking.demo.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.PhoneUser;

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
        if(this.users.get(phone) != null){
            return null; //User exists
        }

        PhoneUser tmp = new PhoneUser(phone);
        this.users.put(phone, tmp);

        return tmp;
    }

    public PhoneUser saveUser(PhoneUser user){
        this.users.put(user.getPhoneNum(), user);
        return user;
    }



    @Override
    public String toString() {

        try {
            ObjectMapper mapper = new ObjectMapper();

            //Need this to parse date or else we get error
            /*
            * Error converting to JSON: Java 8 date/time type `java.time.LocalDateTime` not supported by default:
            * add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable
            * */
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());  // Register the JavaTimeModule
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional: disable writing dates as timestamps


            return mapper.writeValueAsString(this.users);
        } catch (Exception e) {
            return "Error converting to JSON: " + e.getMessage();
        }
    }
}
