package com.lddemo.javaapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.LDClient;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class IndexController {
    @Value("${spring.application.name}")
    String appName;

    @GetMapping("/")
    public String homePage(Model model, HttpServletRequest request) {
        LDClient client = LdClient.getClient();
        LdClient instance = LdClient.getInstance();
        LDContext context = instance.getContext() != null ? instance.getContext()
                : LDContext.create("anonymous");
        var showHomePageSlider = client.boolVariation("release-home-page-slider", context, false);
        var showCoffeePromo1 = client.boolVariation("coffee-promo-1", context, false);
        var showCoffeePromo2 = client.boolVariation("coffee-promo-2", context, false);
        String urlPath = request.getRequestURI();
        model.addAttribute("currentPage", urlPath);
        model.addAttribute("showHomePageSlider", showHomePageSlider);
        model.addAttribute("showCoffeePromo1", showCoffeePromo1);
        model.addAttribute("showCoffeePromo2", showCoffeePromo2);
        return "index";
    }
}
