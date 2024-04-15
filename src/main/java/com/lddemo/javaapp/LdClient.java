package com.lddemo.javaapp;

import com.launchdarkly.sdk.server.*;

public class LdClient {

    private static LdClient instance;

    private LDClient client;

    public LdClient() {
        client = new LDClient(System.getenv("LD_SDK_KEY"));
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
