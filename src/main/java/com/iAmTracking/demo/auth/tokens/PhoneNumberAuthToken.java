package com.iAmTracking.demo.auth.tokens;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class PhoneNumberAuthToken extends AbstractAuthenticationToken {
    private final String principal;

    private final String credential;

    public PhoneNumberAuthToken(String principal, String credential) {
        super(null);
        this.principal = principal;
        this.credential = credential;
    }


    @Override
    public Object getCredentials() {
        return this.credential;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
