package com.iAmTracking.demo.db;

import com.iAmTracking.demo.Message;
import com.iAmTracking.demo.PhoneUser;
import com.iAmTracking.demo.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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
                HashMap<LocalDateTime, ArrayList<Message>> convo = new HashMap<>();
                ArrayList<Message> msgList = new ArrayList<>();
                msgList.add(new Message("8312060419", "Howdy partner"));

                LocalDateTime now = LocalDateTime.now();

                convo.put(now, msgList);
                currentUser = new PhoneUser(username, convo);
        }
        return new PhoneUser(currentUser);
    }

    //Register here
    public void register(){

    }
}
