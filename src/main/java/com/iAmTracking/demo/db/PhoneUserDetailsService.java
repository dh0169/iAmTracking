package com.iAmTracking.demo.db;

import com.iAmTracking.demo.Message;
import com.iAmTracking.demo.PhoneUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PhoneUserDetailsService implements UserDetailsService {

    //Database stuff here
    private final PhoneRepository phoneRepo;

    public PhoneUserDetailsService(PhoneRepository phoneRepo){
        this.phoneRepo = phoneRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PhoneUser currentUser = this.phoneRepo.findByPhone(username);
        if(currentUser == null){
                ConcurrentHashMap<LocalDate, List<Message>> convo = new ConcurrentHashMap<>();
                List<Message> msgList = Collections.synchronizedList(new ArrayList<Message>());
                msgList.add(new Message(username, "This is a new user"));

                LocalDate now = LocalDate.now();

                convo.put(now, msgList);
                currentUser = new PhoneUser(username, convo);
        }
        return new PhoneUser(currentUser);
    }

    //Register here
    public void register(){

    }
}
