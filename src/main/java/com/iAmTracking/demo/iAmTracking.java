package com.iAmTracking.demo;

import jakarta.servlet.http.HttpSession;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.time.LocalDate;

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
    public String subscribed(@RequestParam long phone, RedirectAttributes ra) {
        // Add phone number as a flash attribute for redirect
        ra.addFlashAttribute("phoneNumber", phone);
        return "redirect:/journalDashboard";
    }

    @GetMapping("/journalDashboard")
    public String journalDashboard(Model model, @ModelAttribute("phoneNumber") Long phoneNumber) {
        // Pass phone number to the view model
        if (phoneNumber != null) {
            model.addAttribute("phoneNumber", phoneNumber);
        }
        return "journalDashboard";
    }


    @PostMapping("/retrieveJournalEntry")
    public String retrieveJournalEntry(@RequestParam("entryDate") LocalDate entryDate,
                                       @RequestParam("phoneNumber") String phoneNumber) {
        System.out.println("Entry Date: " + entryDate);
        System.out.println("Phone Number: " + phoneNumber);

        return "redirect:/journalDashboard"; // Redirect to the journal dashboard page
    }

}
