package com.lddemo.javaapp;

import java.io.*;
import java.util.Properties;

import com.launchdarkly.sdk.*;
import com.launchdarkly.sdk.server.*;

public class LdClient {

    private static LdClient instance;

    private LDClient client;

    public LdClient() {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String propsFile = rootPath + "ldclient.properties";
        Properties appProps = new Properties();
        try {
            InputStream in = new FileInputStream(propsFile);
            appProps.load(in);
            in.close();
        } catch (Exception e) {
            System.out.println(propsFile + " is missing from the resources directory.");
        }

        client = new LDClient(appProps.getProperty("sdkkey"));
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

    public static void setContext(LDContext context) {
        String flag = "test-flag";
        LDClient client = getInstance().client;
        client.getFlagTracker().addFlagValueChangeListener(flag, context, event -> {
            System.out.printf("Flag \"%s\" for context \"%s\" has changed from %s to %s\n", event.getKey(),
                    context.getKey(), event.getOldValue(), event.getNewValue());
        });
    }
}
