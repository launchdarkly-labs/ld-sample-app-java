package com.lddemo.javaapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.LDClient;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiRestController {

    @GetMapping("/banner")
    public String getBannerText() {
        LDClient client = LdClient.getClient();
        LdClient instance = LdClient.getInstance();
        LDContext context = instance.getContext() != null ? instance.getContext()
                : LDContext.create("anonymous");
        var bannerText = client.stringVariation("banner-text", context, "No banner text found!");
        JSONObject jo = new JSONObject();
        jo.put("primaryBanner", bannerText);
        return jo.toString();
    }
}