package com.github.ryanp102694.pubgtelemetryparser;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;


/*
 *
 *
 */
@SpringBootApplication
@EnableAsync
public class PubgTelemetryParserApplication implements CommandLineRunner {

	@Value("${build.training.data}")
	Boolean buildTrainingData;

	@Autowired
	BatchTelemetryProcessor batchTelemetryProcessor;

	public static void main(String[] args) {
		SpringApplication.run(PubgTelemetryParserApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if(buildTrainingData){
			try{
				batchTelemetryProcessor.process();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}
