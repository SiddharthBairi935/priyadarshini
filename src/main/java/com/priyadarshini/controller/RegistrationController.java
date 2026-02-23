package com.priyadarshini.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.priyadarshini.model.EventRegistration;

@Controller
public class RegistrationController {

    // This method handles the request when someone visits "http://localhost:8080/register"
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // We pass an empty EventRegistration object to the form so it knows what fields to expect
        model.addAttribute("registration", new EventRegistration());
        
        // This tells Spring to look for an HTML file named "registration-form.html"
        return "registration-form"; 
    }
}