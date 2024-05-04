package com.iAmTracking.demo.auth.filters;

import com.iAmTracking.demo.auth.tokens.PhoneNumberAuthToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneAuthFilter extends AbstractAuthenticationProcessingFilter {

    public PhoneAuthFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String phoneNumber = obtainPhoneNumber(request);
        String otp = obtainOtp(request);

        if (phoneNumber == null) {
            phoneNumber = "";
        }

        if (otp == null) {
            otp = "";
        }

        System.out.println();
        System.out.println();
        System.out.println(PhoneAuthFilter.class);
        System.out.println("Phone number: "+ phoneNumber);
        System.out.println("Otp: "+ otp);
        System.out.println();
        System.out.println();

        PhoneNumberAuthToken authRequest = new PhoneNumberAuthToken(phoneNumber, otp);
        return this.getAuthenticationManager().authenticate(authRequest);
    }


    public static String obtainPhoneNumber(HttpServletRequest request) {
        String rawPhone = request.getParameter("phone");
        if (rawPhone != null) {
            Matcher matcher = Pattern.compile("\\+?\\d{10}").matcher(rawPhone);
            if (matcher.find()) {
                return matcher.group(0);
            }
        }
        return "";
    }

    public static String obtainPhoneNumber(String number) {
        String rawPhone = number.strip();
        if (rawPhone != null) {

            Matcher matcher = Pattern.compile("\\d{10}$").matcher(rawPhone);
            if (matcher.find()) {
                return matcher.group(0);
            }
        }
        return "";
    }

    public static String obtainOtp(HttpServletRequest request) {
        String rawOtp = request.getParameter("otp");
        if (rawOtp != null) {
            StringBuilder filteredOtp = new StringBuilder();
            Matcher matcher = Pattern.compile("\\d").matcher(rawOtp);
            while (matcher.find()) {
                filteredOtp.append(matcher.group());
                if (filteredOtp.length() == 8) {
                    break; // Stop after collecting 8 digits
                }
            }
            return filteredOtp.toString();
        }
        return "";
    }
}
