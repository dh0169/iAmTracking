package com.iAmTracking.demo;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class SupabaseUserReg {
    private static final String SUPABASE_URL = "https://pzzbasaitpeaelgtbkbw.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB6emJhc2FpdHBlYWVsZ3Ria2J3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTMwNTk2OTQsImV4cCI6MjAyODYzNTY5NH0.fxLIlleOtAaqbrYiELRMxz_1DFjDbptLZ-zIfTi_zbM";

    public static void main(String[] args) throws IOException {
        signUpNewUser("example@email.com", "example-password123");
    }

    public static void signUpNewUser(String email, String password) throws IOException {
        URL url = new URL(SUPABASE_URL + "/auth/v1/signup");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("apikey", SUPABASE_API_KEY);
        connection.setDoOutput(true);

        // Construct request body for user sign up
        String requestBody = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"options\":{\"emailRedirectTo\":\"https://example.com/welcome\"}}";
        connection.getOutputStream().write(requestBody.getBytes());

        int responseCode = connection.getResponseCode();
        System.out.println("Sign-up response code: " + responseCode);

        // Read response body
        Scanner scanner = new Scanner(connection.getInputStream());
        StringBuilder responseBody = new StringBuilder();
        while (scanner.hasNextLine()) {
            responseBody.append(scanner.nextLine());
        }
        System.out.println("Sign-up response body: " + responseBody.toString());

        scanner.close();
        connection.disconnect();
    }

    /*
    Can you create these functions in SupabaseUserReg.java?
    They should interact with the supabase db.

    createNewUser(String number) -> create new PhoneUser in db and return user or Null on fail;
    findByPhone(String number) -> find PhoneUser in db and return new PhoneUser or Null on fail;
    saveUser(PhoneUser user) -> Save user to db and return the saved user or Null on fail;


    Here is what my PhoneUser class looks like. This should help when you create new PhoneUser objects

    public class PhoneUser{
        private String phoneNum;
        private Map<Date, ArrayList<Message>> conversations; //Conversation with ChatGPT

        public PhoneUser(String phoneNum, Map<Date, ArrayList<Message>> conversations)

        public PhoneUser(PhoneUser user)

        public void setPhoneNum(String phoneNum)

        public void setConversations(Map<Date, ArrayList<Message>> conversations)

        public String getPhoneNum()

        public Map<Date, ArrayList<Message>> getConversations()
    }

    */

}