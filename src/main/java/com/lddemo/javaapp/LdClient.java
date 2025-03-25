package com.lddemo.javaapp;

import com.launchdarkly.sdk.server.*;

public class LdClient {

    private static LdClient instance;

    private LDClient client;

    public LdClient() {
        String sdkKey = System.getenv("LD_SDK_KEY");
        client = new LDClient(sdkKey);
    }

    private static LdClient getInstance() {
        if (instance == null) {
            instance = new LdClient();
        }

        return instance;
    }

    public static LDClient getClient() {
        return getInstance().client;
    }
}
