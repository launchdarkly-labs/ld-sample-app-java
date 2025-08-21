package com.lddemo.javaapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ContactController {
    @Value("${spring.application.name}")
    String appName;

    @GetMapping("/contact")
    public String contactPage(Model model, HttpServletRequest request) {
        String urlPath = request.getRequestURI();
        model.addAttribute("currentPage", urlPath);
        return "contact";
    }
}
