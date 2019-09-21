package com.github.ryanp102694.pubgtelemetryparser;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.event.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class TelemetryProcessor {

    private final static Logger log = LoggerFactory.getLogger(TelemetryProcessor.class);


    Map<String, TelemetryEventHandler> telemetryEventHandlerMap;

    public TelemetryProcessor(){
        Map<String, TelemetryEventHandler> telemetryEventHandlerMap = new HashMap<>();
        telemetryEventHandlerMap.put("LogMatchDefinition", new MatchDefinitionEventHandler());
        telemetryEventHandlerMap.put("LogMatchStart", new MatchStartEventHandler());
        telemetryEventHandlerMap.put("LogMatchEnd", new MatchEndEventHandler());
        telemetryEventHandlerMap.put("LogPlayerPosition", new PlayerPositionEventHandler());
        telemetryEventHandlerMap.put("LogParachuteLanding", new ParachuteLandingEventHandler());
        telemetryEventHandlerMap.put("LogGameStatePeriodic", new GameStatePeriodicEventHandler());
        telemetryEventHandlerMap.put("LogPlayerKill", new PlayerKillEventHandler());
        this.telemetryEventHandlerMap = telemetryEventHandlerMap;
    }


    @Async
    public CompletableFuture<GameData> process(InputStream telemetry) throws IOException {
        log.debug("Begin processing telemetry");
        long startTime = System.currentTimeMillis();
        GameData gameData = new GameData();
        JSONArray telemetryEvents;
        telemetryEvents = new JSONArray(IOUtils.toString(telemetry, StandardCharsets.UTF_8));
        for(int i = 0; i < telemetryEvents.length(); i++) {
            JSONObject telemetryEvent = telemetryEvents.getJSONObject(i);
            String eventType = telemetryEvent.getString("_T");

            if(telemetryEventHandlerMap.get(eventType) != null){
                telemetryEventHandlerMap.get(eventType).handle(telemetryEvent, gameData);
            }
        }
        log.debug("End processing, took {} milliseconds", System.currentTimeMillis() - startTime);
        return CompletableFuture.completedFuture(gameData);
    }
}
