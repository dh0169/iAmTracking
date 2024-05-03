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

        StringBuilder stringBuilder = new StringBuilder();
        for(String splt : otp.split("-")){
            stringBuilder.append(splt);
        }
        otp = stringBuilder.toString();

        System.out.println("\n\n"+otp+"\n\n");
        phoneNumber = phoneNumber.trim();
        PhoneNumberAuthToken authRequest = new PhoneNumberAuthToken(phoneNumber, otp);


        return this.getAuthenticationManager().authenticate(authRequest);
    }


    protected String obtainPhoneNumber(HttpServletRequest request) {
        return request.getParameter("phone");
    }

    protected String obtainOtp(HttpServletRequest request) {
        return request.getParameter("otp");
    }

}
