package com.github.ryanp102694.pubgtelemetryparser.controller;

import com.github.ryanp102694.pubgtelemetryparser.TelemetryProcessor;
import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("pubgml")
public class PubgPredictionController {

    @Autowired
    TelemetryProcessor telemetryProcessor;

    @GetMapping("/prediction")
    String getString(@RequestParam("telemetryUrl") String telemetryUrl) throws IOException {

        Map<String, TelemetryEventHandler> telemetryEventHandlerMap = new HashMap<>();
        telemetryEventHandlerMap.put("LogMatchDefinition", new MatchDefinitionEventHandler());
        telemetryEventHandlerMap.put("LogMatchStart", new MatchStartEventHandler());
        telemetryEventHandlerMap.put("LogPlayerPosition", new PlayerPositionEventHandler());
        telemetryEventHandlerMap.put("LogParachuteLanding", new ParachuteLandingEventHandler());
        telemetryEventHandlerMap.put("LogGameStatePeriodic", new GameStatePeriodicEventHandler());


        GameData gameData = telemetryProcessor.process(new UrlResource(telemetryUrl).getInputStream()).join();


        //download telemetry file
        //build query arrays
        //hit tensorflow model server and get predictions
        //create response and send to client
        return "Hello World";
    }

}
