package com.lddemo.javaapp;

import com.launchdarkly.sdk.LDContext;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@SpringBootApplication
public class JavaappApplication {

	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		System.out.printf("\n\n\nLaunchDarkly is ready!\n");
	}

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		String clientKey = dotenv.get("LD_CLIENT_KEY");
		String fileName = "src/main/resources/static/js/keys.js";
		String content = "const clientKey = \"" + clientKey + "\";\n";

		try (FileWriter fw = new FileWriter(fileName);
				BufferedWriter bw = new BufferedWriter(fw)) {
			bw.write(content);
			System.out.println("Content successfully written to " + fileName);
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}

		LdClient instance = LdClient.getInstance();
		LDContext userContext = LDContext.builder("user-018e7bd4-ab96-782e-87b0-b1e32082b481")
				.kind("user")
				.name("Miriam Wilson")
				.set("language", "en")
				.set("tier", "premium")
				.set("userId", "mwilson")
				.set("role", "developer")
				.set("email", "miriam.wilson@example.com")
				.build();
		LDContext devContext = LDContext.builder("device-018e7bd4-ab96-782e-87b0-b1e32082b481")
				.kind("device")
				.set("os", "macOS")
				.set("osVersion", "15.6")
				.set("model", "MacBook Pro")
				.set("manufacturer", "Apple")
				.build();
		LDContext context = LDContext.multiBuilder()
				.add(userContext)
				.add(devContext)
				.build();

		instance.setContext(context);
		// String flag = "test-flag";
		// LDClient client = LdClient.getClient();
		// LDContext context = LDContext.builder("018e5dc7-c137-7a1f-bf20-406d17127f8b")
		// .kind("device")
		// .name("Linux")
		// .build();

		// client.getFlagTracker().addFlagValueChangeListener(flag, context, event -> {
		// System.out.printf("Flag \"%s\" for context \"%s\" has changed from %s to
		// %s\n", event.getKey(),
		// context.getKey(), event.getOldValue(), event.getNewValue());
		// });

		SpringApplication.run(JavaappApplication.class, args);
	}

}
