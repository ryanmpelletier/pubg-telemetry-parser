package com.github.ryanp102694.pubgtelemetryparser;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.model.TrainingResult;
import com.github.ryanp102694.pubgtelemetryparser.service.TrainingDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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

    public TrainingResult process() throws IOException {

        TrainingResult trainingResult = new TrainingResult();
        long startTime = System.currentTimeMillis();

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
                        .map(gameData -> {

                            Stream<Map.Entry<String, SortedMap<String, String>>> result =
                                    Stream.of(
                                            gameData.getPlayerDataPoints("1.0").entrySet().stream(),
                                            gameData.getPlayerDataPoints("2.0").entrySet().stream(),
                                            gameData.getPlayerDataPoints("3.0").entrySet().stream(),
                                            gameData.getPlayerDataPoints("4.0").entrySet().stream(),
                                            gameData.getPlayerDataPoints("5.0").entrySet().stream(),
                                            gameData.getPlayerDataPoints("6.0").entrySet().stream(),
                                            gameData.getPlayerDataPoints("7.0").entrySet().stream()
                                    ).flatMap(Function.identity());

                            return trainingDataWriter.writeGameDataPoints(gameData.getMatchUUID(), result.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                        })
                        .collect(Collectors.toList())
                        .stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        log.debug("Finished writing data, processed {} telemetry files.", gameDatas.size());

        trainingResult.setFilesProcessed(gameDatas.size());
        trainingResult.setTrainingDataDirectory(telemetryInputDirectory);
        trainingResult.setTimeElapsed(System.currentTimeMillis() - startTime);

        return trainingResult;
    }

}
