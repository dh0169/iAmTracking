package com.iAmTracking.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;

@Controller
@SpringBootApplication
public class iAmTracking {

    public static void main(String[] args) {
        SpringApplication.run(iAmTracking.class, args);
    }


    @GetMapping("/")
    public String index(Model model){
        return "index";
    }


    @PostMapping("/subscribed")
    //RequestBody must be filled or will return 400. puts entire body into variable
    //RequestParam just takes in a single parameter and throws a 400 if wrong type, i.e long
    public String subscribed(@RequestParam long phone, Model model, RedirectAttributes ra) {
        FileWriter writer;
        try {
            //phone = phone.substring(phone.indexOf("=") + 1); //TODO might throw an error. Find better way.
            //Integer.parseInt(phone); // TODO parse phone using regex
            File emailList = ResourceUtils.getFile("email-list.txt");
            if (!emailList.exists()) {
                writer = new FileWriter(emailList);
            } else {
                writer = new FileWriter(emailList, true); // Append mode
            }

            writer.write(String.format("%s\n", phone));
            writer.close();
        }catch (NumberFormatException e){
            ra.addFlashAttribute("msg", "Please provide phone number only.");
            return "redirect:/";

        }catch (Exception e){
            e.printStackTrace();
            ra.addFlashAttribute("msg", "An error occurred.");
        }

        model.addAttribute("msg", "Success! Please check your phone for a link");
        return "subscribed";
    }

}
