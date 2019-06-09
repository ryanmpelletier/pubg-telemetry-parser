package com.ryanp102694.pubgtelemetryparser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class PubgTelemetryParserApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(PubgTelemetryParserApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("hello world!");

		String jsonString = new String(Files.readAllBytes(Paths.get("telemetry.json")));

		JSONArray jsonData = new JSONArray(jsonString);

		System.out.println(jsonData.getJSONObject(0).keys());

		for(String key : jsonData.getJSONObject(0).keySet()){
			System.out.println(key);
		}
	}
}
