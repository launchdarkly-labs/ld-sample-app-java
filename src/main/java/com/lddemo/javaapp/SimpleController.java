package com.lddemo.javaapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.LDClient;;;

@Controller
public class SimpleController {
    @Value("${spring.application.name}")
    String appName;

    @GetMapping("/")
    public String homePage(Model model) {
        LDClient client = LdClient.getClient();
        LDContext context = LDContext.builder("018e5dc7-c137-7a1f-bf20-406d17127f8b")
                .kind("device")
                .name("Linux")
                .build();
        LdClient.setContext(context);
        boolean flagValue = client.boolVariation("test-flag", context, false);

        model.addAttribute("appName", appName);
        model.addAttribute("flagValue", flagValue);
        return "home";
    }
}
