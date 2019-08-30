package com.github.ryanp102694.pubgtelemetryparser;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.GameDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Component
public class BatchTelemetryProcessor {

    @Value("${data.output.dir}")
    private String dataOutputDir;

    @Value("${data.input.dir}")
    private String telemetryInputDirectory;

    @Autowired
    TelemetryProcessor telemetryProcessor;

    private final static Logger log = LoggerFactory.getLogger(BatchTelemetryProcessor.class);

    public BatchTelemetryProcessor(){}

    void process() throws IOException{
        File folder = new File(telemetryInputDirectory);
        List<CompletableFuture<GameData>> completableFutures = new ArrayList<>();
        for(File telemetryFile : folder.listFiles()) {
            if (telemetryFile.isFile()) {
                completableFutures.add(telemetryProcessor.process(new FileInputStream(telemetryFile)));
            }
        }

        completableFutures.stream()
                .map(CompletableFuture::join)
                .forEach(gameData -> {
                    try{
                        new GameDataWriter(dataOutputDir).writeGameDataPoints(gameData);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                });

        log.debug("Finished writing data, processed {} telemetry files.", completableFutures.size());

    }

}
