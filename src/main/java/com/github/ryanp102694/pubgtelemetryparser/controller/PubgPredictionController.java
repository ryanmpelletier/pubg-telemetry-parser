package com.github.ryanp102694.pubgtelemetryparser.controller;

import com.github.ryanp102694.pubgtelemetryparser.TelemetryProcessor;
import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.event.*;
import com.github.ryanp102694.pubgtelemetryparser.service.PredictionClient;
import com.github.ryanp102694.pubgtelemetryparser.service.PredictionRequestWriter;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;


@RestController
@RequestMapping("pubgml")
public class PubgPredictionController {

    TelemetryProcessor telemetryProcessor;
    PredictionRequestWriter predictionRequestWriter;
    PredictionClient predictionClient;

    public PubgPredictionController(@Autowired TelemetryProcessor telemetryProcessor,
                                    @Autowired PredictionRequestWriter predictionRequestWriter,
                                    @Autowired PredictionClient predictionClient){
        this.telemetryProcessor = telemetryProcessor;
        this.predictionRequestWriter = predictionRequestWriter;
        this.predictionClient = predictionClient;
    }

    @GetMapping("/prediction")
    String getString(@RequestParam("telemetryUrl") String telemetryUrl) throws IOException {

        Map<String, TelemetryEventHandler> telemetryEventHandlerMap = new HashMap<>();
        telemetryEventHandlerMap.put("LogMatchDefinition", new MatchDefinitionEventHandler());
        telemetryEventHandlerMap.put("LogMatchStart", new MatchStartEventHandler());
        telemetryEventHandlerMap.put("LogPlayerPosition", new PlayerPositionEventHandler());
        telemetryEventHandlerMap.put("LogParachuteLanding", new ParachuteLandingEventHandler());
        telemetryEventHandlerMap.put("LogGameStatePeriodic", new GameStatePeriodicEventHandler());

        GameData gameData = telemetryProcessor.process(new GZIPInputStream(new URL(telemetryUrl).openStream())).join();

        String jsonPredictionRequest = predictionRequestWriter.getPredictionString(gameData.getPlayerDataPoints("1.0"));
        return predictionClient.makePrediction(jsonPredictionRequest);
    }

}
