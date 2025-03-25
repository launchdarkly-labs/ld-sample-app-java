package com.lddemo.javaapp;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.LDClient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class JavaappApplication {

	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		System.out.printf("\n\n\nLaunchDarkly is ready!\n");
	}

	public static void main(String[] args) {
		String flag = "test-flag";
		LDClient client = LdClient.getClient();
		LDContext context = LDContext.builder("018e5dc7-c137-7a1f-bf20-406d17127f8b")
				.kind("device")
				.name("Linux")
				.build();

		client.getFlagTracker().addFlagValueChangeListener(flag, context, event -> {
			System.out.printf("Flag \"%s\" for context \"%s\" has changed from %s to %s\n", event.getKey(),
					context.getKey(), event.getOldValue(), event.getNewValue());
		});

		SpringApplication.run(JavaappApplication.class, args);
	}

}
