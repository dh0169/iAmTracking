package com.iAmTracking.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@SpringBootApplication
public class iAmTracking {
    @RequestMapping("/")
    public String index(){
        return "index";
    }


}
