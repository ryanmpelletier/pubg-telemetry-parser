package com.github.ryanp102694.pubgtelemetryparser;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import java.io.File;



/*
 *
 *
 */
@SpringBootApplication
@EnableAsync
public class PubgTelemetryParserApplication implements CommandLineRunner {

	@Value("${build.training.data}")
	Boolean buildTrainingData;

	@Value("${telemetry.input.dir}")
	String telemetryInputDirectory;

	@Autowired
	BatchTelemetryProcessor batchTelemetryProcessor;

	public static void main(String[] args) {
		SpringApplication.run(PubgTelemetryParserApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if(buildTrainingData){
			File folder = new File(telemetryInputDirectory);
			for(File telemetryFile : folder.listFiles()){
				if (telemetryFile.isFile()) {
					batchTelemetryProcessor.process(telemetryFile.getAbsolutePath());
				}
			}
		}
	}
}
