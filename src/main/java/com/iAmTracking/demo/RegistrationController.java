package com.iAmTracking.demo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    @PostMapping("/register")
    public String registerUser(@RequestBody RegistrationRequest request) {
        // Call your method to handle registration logic
        String phone = request.getPhone();
        String password = request.getPassword();

        // Call method to sign up user using Supabase Auth API
        signUpNewUser(phone, password);

        return "Registration successful!";
    }

    // Method to handle user registration using Supabase Auth API
    private void signUpNewUser(String phone, String password) {
        // Implement the logic to call Supabase Auth API for user registration
        // Refer to the Java code provided earlier for signing up a new user with Supabase
    }
}

class RegistrationRequest {
    private String phone;
    private String password;

    // Getters and setters
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}