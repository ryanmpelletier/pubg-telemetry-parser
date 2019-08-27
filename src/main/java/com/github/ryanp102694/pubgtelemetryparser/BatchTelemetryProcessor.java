package com.github.ryanp102694.pubgtelemetryparser;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.GameDataWriter;
import com.github.ryanp102694.pubgtelemetryparser.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
public class BatchTelemetryProcessor {

    @Value("${data.output.dir}")
    private String dataOutputDir;

    private final static Logger log = LoggerFactory.getLogger(BatchTelemetryProcessor.class);

    private Map<String, TelemetryEventHandler> telemetryEventHandlerMap;

    public BatchTelemetryProcessor(){
        Map<String, TelemetryEventHandler> telemetryEventHandlerMap = new HashMap<>();
        telemetryEventHandlerMap.put("LogMatchDefinition", new MatchDefinitionEventHandler());
        telemetryEventHandlerMap.put("LogMatchStart", new MatchStartEventHandler());
        telemetryEventHandlerMap.put("LogPlayerPosition", new PlayerPositionEventHandler());
        telemetryEventHandlerMap.put("LogParachuteLanding", new ParachuteLandingEventHandler());
        telemetryEventHandlerMap.put("LogGameStatePeriodic", new GameStatePeriodicEventHandler());
        this.telemetryEventHandlerMap = telemetryEventHandlerMap;
    }

    @Async
    public void process(String telemetryFilePath){
        long startTime = System.currentTimeMillis();
        log.debug("Begin processing {}", telemetryFilePath);
        GameData gameData = new TelemetryProcessor(telemetryFilePath, dataOutputDir, telemetryEventHandlerMap).process();
        GameDataWriter gameDataWriter = new GameDataWriter(dataOutputDir);
        try{
            log.debug("Writing data points for {}", telemetryFilePath);
            gameDataWriter.writeGameDataPoints(gameData);
        }catch(IOException e){
            log.error("Error writing data points for {}", telemetryFilePath);
            e.printStackTrace();
        }
        log.debug("End processing {}, took {} milliseconds", telemetryFilePath, startTime - System.currentTimeMillis());
    }

    public void setTelemetryEventHandlerMap(Map<String, TelemetryEventHandler> telemetryEventHandlerMap) {
        this.telemetryEventHandlerMap = telemetryEventHandlerMap;
    }
}
