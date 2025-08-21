package com.lddemo.javaapp;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.*;

import io.github.cdimascio.dotenv.Dotenv;

public class LdClient {

    private static LdClient instance;
    private LDContext context;
    private LDClient client;

    public LdClient() {
        Dotenv dotenv = Dotenv.load();
        String sdkKey = dotenv.get("LD_SDK_KEY");
        client = new LDClient(sdkKey);
    }

    public static LdClient getInstance() {
        if (instance == null) {
            instance = new LdClient();
        }

        return instance;
    }

    public static LDClient getClient() {
        return getInstance().client;
    }

    public void setContext(LDContext context) {
        this.context = context;
    }

    public LDContext getContext() {
        return context;
    }
}
