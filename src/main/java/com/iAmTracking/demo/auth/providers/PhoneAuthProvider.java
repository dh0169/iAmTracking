package com.iAmTracking.demo.auth.providers;

import com.iAmTracking.demo.OneTimePasscode;
import com.iAmTracking.demo.auth.tokens.PhoneNumberAuthToken;
import com.iAmTracking.demo.db.OTPRepository;
import com.iAmTracking.demo.db.PhoneUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class PhoneAuthProvider  implements AuthenticationProvider {

    @Autowired
    private PhoneUserDetailsService userDetailsService;

    @Autowired
    OTPRepository otpRepository;

    public PhoneAuthProvider(PhoneUserDetailsService userDetailsService, OTPRepository otpRepository) {
        this.userDetailsService = userDetailsService;
        this.otpRepository = otpRepository;
    }

    //Implement SMS API service here



    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if(!(authentication instanceof PhoneNumberAuthToken)){
            return null;
        }

        String phone = (String) authentication.getPrincipal();
        String otpCode = (String) authentication.getCredentials();

        //Auth logic here
        //Return PhoneNumberAuthToken or Nil
        UserDetails phoneUser = userDetailsService.loadUserByUsername(phone);
        OneTimePasscode otp = otpRepository.getCode(phone);

        if(phoneUser != null && otp != null){
            System.out.println("phoneUser: "+ phoneUser + "\n\n");

            if (otp.isExpired()){
                System.out.println("\n\nEXPIRED OTP CODE\n\n");

                throw new CredentialsExpiredException("OTP Expired");
            }else if(!otp.getCode().equals(otpCode)){
                System.out.println("\n\nBAD OTP CODE\n\n");
                System.out.println(otpCode);
                System.out.println(otp.getCode());
                throw new BadCredentialsException("Incorrect OTP Code");
            }

            System.out.println("\n\nWE GOT A MATCH\n\n");


            PhoneNumberAuthToken phoneNumberAuthToken = new PhoneNumberAuthToken(phone, otpCode);
//            phoneNumberAuthToken.setAuthenticated(true);
            System.out.println("phoneNumberAuthToken: "+ phoneNumberAuthToken + "\n\n");

            return phoneNumberAuthToken;
        }

        System.out.println("No Match");
        System.out.println("phoneUser: "+ phoneUser + "\n\n");


        throw new UsernameNotFoundException("Invalid phonenumber");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PhoneNumberAuthToken.class.isAssignableFrom(authentication);
    }
}

