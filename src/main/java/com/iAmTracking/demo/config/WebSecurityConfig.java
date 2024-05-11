package com.iAmTracking.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iAmTracking.demo.OneTimePasscode;
import com.iAmTracking.demo.PhoneUser;
import com.iAmTracking.demo.service.GPTApi;
import com.iAmTracking.demo.service.SMSApi;
import com.iAmTracking.demo.auth.filters.PhoneAuthFilter;
import com.iAmTracking.demo.auth.handlers.PhoneAuthenticationHandler;
import com.iAmTracking.demo.auth.providers.PhoneAuthProvider;
import com.iAmTracking.demo.db.OTPRepository;
import com.iAmTracking.demo.db.PhoneRepository;
import com.iAmTracking.demo.db.PhoneUserDetailsService;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaResult;
import io.github.amithkoujalgi.ollama4j.core.models.chat.OllamaChatRequestBuilder;
import io.github.amithkoujalgi.ollama4j.core.types.OllamaModelType;
import io.github.amithkoujalgi.ollama4j.core.utils.OptionsBuilder;
import io.github.amithkoujalgi.ollama4j.core.utils.PromptBuilder;
import io.github.amithkoujalgi.ollama4j.core.utils.SamplePrompts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig{

    @Value("${spring.datasource.SMS_API_URL}")
    private String SMS_API_URL;

    @Value("${spring.datasource.SMS_API_KEY}")
    private String SMS_API_KEY;


    @Value("${spring.datasource.GPT_API_URL}")
    private String GPT_API_URL;

    @Value("${spring.datasource.GPT_API_KEY}")
    private String GPT_API_KEY;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                )
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/journalDashboard").authenticated() // Ensure that dashboard is protected
                        .anyRequest().permitAll()
                )
                .formLogin(login -> login
                        .loginPage("/")
                        .loginProcessingUrl("/login")
                )
                .logout(logout -> logout
                        .addLogoutHandler(clearSiteData())
                        .logoutSuccessUrl("/?logout")
                        .permitAll()
                )
                .addFilterAt(oneTimePasswordAuthFilter(authenticationManager(), phoneUserDetailsService()), UsernamePasswordAuthenticationFilter.class)

                .csrf().disable()

        ;

        return http.build();
    }

    @Bean
    HeaderWriterLogoutHandler clearSiteData(){
        HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL));
        return clearSiteData;
    }

    @Bean
    public PhoneAuthFilter oneTimePasswordAuthFilter(AuthenticationManager authMan, PhoneUserDetailsService phoneUserDetailsService){
        PhoneAuthFilter  filter = new PhoneAuthFilter(new AntPathRequestMatcher("/login", "POST"));
        filter.setAuthenticationManager(authMan);
        filter.setAuthenticationSuccessHandler(new PhoneAuthenticationHandler("/journalDashboard", securityContextRepository(), otpRepository()));  // Redirect on success
        filter.setAuthenticationFailureHandler(new PhoneAuthenticationHandler("/logout", securityContextRepository(), otpRepository()));

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
//        PhoneUser customUser = new PhoneUser("8312060419"); //Sample user
//        Map<String, PhoneUser> phoneToCustomUser = new HashMap<>();
//        phoneToCustomUser.put(customUser.getPhoneNum(), customUser);


        return new PhoneRepository();
    }

    @Bean
    OTPRepository otpRepository(){
        return new OTPRepository();
    }


    @Bean
    GPTApi gptApi(){
        return new GPTApi(GPT_API_URL, GPT_API_KEY, objectMapper());
    }


    @Bean
    ObjectMapper objectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());  // Register the JavaTimeModule
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional: disable writing dates as timestamps

        return mapper;
    }

    @Bean
    SMSApi smsApi(){ return new SMSApi(this.SMS_API_URL, this.SMS_API_KEY, new ObjectMapper());}
    @Bean
    ConcurrentHashMap<Integer, Boolean> getMap(){ return new ConcurrentHashMap<>();}

    @Bean
    OllamaAPI ollamaAPI(){
        OllamaAPI llama = new OllamaAPI("http://127.0.0.1:11434");
        llama.setRequestTimeoutSeconds(30);
        return llama;
    }

    @Bean
    OllamaChatRequestBuilder ollamaChatRequestBuilder(){
        return OllamaChatRequestBuilder.getInstance("llama3");
    }

}