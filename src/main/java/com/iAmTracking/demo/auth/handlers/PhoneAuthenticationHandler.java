package com.iAmTracking.demo.auth.handlers;

import com.iAmTracking.demo.OneTimePasscode;
import com.iAmTracking.demo.auth.tokens.PhoneNumberAuthToken;
import com.iAmTracking.demo.db.OTPRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;

/**
 * An authentication handler that saves an authentication either way.
 *
 * The reason for this is so that the rest of the factors are collected, even if earlier
 * factors failed.
 *
 * @author Josh Cummings
 */
public class PhoneAuthenticationHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private final AuthenticationSuccessHandler successHandler;

    private final AuthenticationFailureHandler failureHandler;

    private final SecurityContextRepository securityContextRepository;
    private final OTPRepository otpRepository;


    public PhoneAuthenticationHandler(String url, SecurityContextRepository securityContextRepository, OTPRepository otpRepository) {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler(url);
        successHandler.setAlwaysUseDefaultTargetUrl(true);
        this.successHandler = successHandler;

        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler(url);
        this.failureHandler = failureHandler;


        this.securityContextRepository = securityContextRepository;

        this.otpRepository = otpRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException, BadCredentialsException {
//        Authentication anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
//                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        //saveMfaAuthentication(request, response, anonymous);
        this.failureHandler.onAuthenticationFailure(request, response, new UsernameNotFoundException(""));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        saveMfaAuthentication(request, response, authentication);
        this.successHandler.onAuthenticationSuccess(request, response, authentication);
    }

    private void saveMfaAuthentication(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.getContext();
        if (authentication instanceof PhoneNumberAuthToken){
            String phoneNumber = (String) authentication.getPrincipal();
            String otp = (String) authentication.getCredentials();

            OneTimePasscode otpCode =  this.otpRepository.removeCode(phoneNumber);
            System.out.println("\n\n\nSuccessfull Login, removing OTPCode: " + otpCode.getCode());


            PhoneNumberAuthToken token = new PhoneNumberAuthToken(phoneNumber, otp);
            token.setAuthenticated(true);
            context.setAuthentication(token);
            this.securityContextRepository.saveContext(context, request, response);
        }
    }

}