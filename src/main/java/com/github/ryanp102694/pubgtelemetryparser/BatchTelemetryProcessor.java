package com.github.ryanp102694.pubgtelemetryparser;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.TrainingDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class BatchTelemetryProcessor {

    @Value("${data.input.dir}")
    private String telemetryInputDirectory;

    @Autowired
    TelemetryProcessor telemetryProcessor;

    @Autowired
    TrainingDataWriter trainingDataWriter;

    private final static Logger log = LoggerFactory.getLogger(BatchTelemetryProcessor.class);

    public BatchTelemetryProcessor(){}

    void process() throws IOException {

        File folder = new File(telemetryInputDirectory);
        List<GameData> gameDatas =
                Stream.of(folder.listFiles())
                .filter(File::isFile)
                .map(telemetryFile -> {
                    try{
                        return telemetryProcessor.process(new FileInputStream(telemetryFile));
                    }catch(IOException e){
                        log.debug("There was a problem opening {}", telemetryFile.getAbsolutePath());
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

                gameDatas.stream()
                        .map(gameData -> trainingDataWriter
                                .writeGameDataPoints(gameData.getMatchUUID(), gameData.getPlayerDataPoints("1.0")))
                        .collect(Collectors.toList())
                        .stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());



        log.debug("Finished writing data, processed {} telemetry files.", gameDatas.size());

    }

}
