package com.iAmTracking.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.PhoneUser;
import com.iAmTracking.demo.SMSApi;
import com.iAmTracking.demo.auth.filters.PhoneAuthFilter;
import com.iAmTracking.demo.auth.handlers.PhoneAuthenticationHandler;
import com.iAmTracking.demo.auth.providers.PhoneAuthProvider;
import com.iAmTracking.demo.db.OTPRepository;
import com.iAmTracking.demo.db.PhoneRepository;
import com.iAmTracking.demo.db.PhoneUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig{

    @Value("${spring.datasource.SMS_API_URL}")
    private String SMS_API_URL;

    @Value("${spring.datasource.SMS_API_KEY}")
    private String SMS_API_KEY;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                )
                .csrf().disable()
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/login", "/src/**").permitAll() // Assuming /src/** are your static resources
                        .requestMatchers("/journalDashboard").authenticated() // Ensure that dashboard is protected
                        .anyRequest().authenticated()
                )
                .addFilterAt(oneTimePasswordAuthFilter(authenticationManager(), phoneUserDetailsService()), UsernamePasswordAuthenticationFilter.class)
                .formLogin(login -> login
                        .loginPage("/")
                        .loginProcessingUrl("/login")
                );

        return http.build();
    }

    @Bean
    public PhoneAuthFilter oneTimePasswordAuthFilter(AuthenticationManager authMan, PhoneUserDetailsService phoneUserDetailsService){
        PhoneAuthFilter  filter = new PhoneAuthFilter(new AntPathRequestMatcher("/login", "POST"));
        filter.setAuthenticationManager(authMan);
        filter.setAuthenticationSuccessHandler(new PhoneAuthenticationHandler("/journalDashboard", securityContextRepository()));  // Redirect on success
        filter.setAuthenticationFailureHandler(new PhoneAuthenticationHandler("/logout", securityContextRepository()));

        return filter;
    }


    @Bean
    SecurityContextRepository securityContextRepository(){
        SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
        return securityContextRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return new ProviderManager(List.of(new PhoneAuthProvider(phoneUserDetailsService(), otpRepository())));
    }

    @Bean
    PhoneUserDetailsService phoneUserDetailsService(){
        PhoneRepository repo = phoneRepository();
        return new PhoneUserDetailsService(repo);
    }

    @Bean
    PhoneRepository phoneRepository() {
        // the hashed password was calculated using the following code
        // the hash should be done up front, so malicious users cannot discover the
        // password
        // PasswordEncoder encoder =
        // PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // String encodedPassword = encoder.encode("password");

        // the raw password is "password"
        String encodedPassword = "{bcrypt}$2a$10$h/AJueu7Xt9yh3qYuAXtk.WZJ544Uc2kdOKlHu2qQzCh/A3rq46qm";

        // to sync your phone with the Google Authenticator secret, hand enter the value
        // in base32Key
        // String base32Key = "QDWSM3OYBPGTEVSPB5FKVDM3CSNCWHVK";
        // Base32 base32 = new Base32();
        // byte[] b = base32.decode(base32Key);
        // String secret = Hex.encodeHexString(b);

        String hexSecret = "80ed266dd80bcd32564f0f4aaa8d9b149a2b1eaa";
        //String encrypted = new String(Hex.encode(encryptor.encrypt(hexSecret.getBytes())));

        // the raw security answer is "smith"
        String encodedSecurityAnswer = "{bcrypt}$2a$10$JIXMjAszy3RUu8y5T0zH0enGJCGumI8YE.K7w3wsM5xXDfeVIsJhq";

        //PhoneUser customUser = new PhoneUser("8312060419");
        Map<String, PhoneUser> phoneToCustomUser = new HashMap<>();
        //phoneToCustomUser.put(customUser.getPhoneNum(), customUser);
        return new PhoneRepository(phoneToCustomUser);
    }

    @Bean
    OTPRepository otpRepository(){
        return new OTPRepository();
    }

    @Bean
    SMSApi smsApi(){ return new SMSApi(this.SMS_API_URL, this.SMS_API_KEY, new ObjectMapper());}

}
