package com.github.ryanp102694.pubgtelemetryparser.controller;

import com.github.ryanp102694.pubgtelemetryparser.BatchTelemetryProcessor;
import com.github.ryanp102694.pubgtelemetryparser.TelemetryProcessor;
import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.model.Prediction;
import com.github.ryanp102694.pubgtelemetryparser.data.model.TrainingResult;
import com.github.ryanp102694.pubgtelemetryparser.event.*;
import com.github.ryanp102694.pubgtelemetryparser.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.zip.GZIPInputStream;


@RestController
@RequestMapping("pubgml")
public class PubgPredictionController {

    @Value("${build.training.data}")
    Boolean buildTrainingData;

    BatchTelemetryProcessor batchTelemetryProcessor;
    TelemetryProcessor telemetryProcessor;
    PredictionService predictionService;

    public PubgPredictionController(@Autowired BatchTelemetryProcessor batchTelemetryProcessor,
                                    @Autowired TelemetryProcessor telemetryProcessor,
                                    @Autowired PredictionService predictionService){
        this.batchTelemetryProcessor = batchTelemetryProcessor;
        this.telemetryProcessor = telemetryProcessor;
        this.predictionService = predictionService;
    }

    @GetMapping("/prediction")
    ResponseEntity<Prediction> getPrediction(@RequestParam("telemetryUrl") String telemetryUrl) throws IOException {

        Map<String, TelemetryEventHandler> telemetryEventHandlerMap = new HashMap<>();
        telemetryEventHandlerMap.put("LogMatchDefinition", new MatchDefinitionEventHandler());
        telemetryEventHandlerMap.put("LogMatchStart", new MatchStartEventHandler());
        telemetryEventHandlerMap.put("LogPlayerPosition", new PlayerPositionEventHandler());
        telemetryEventHandlerMap.put("LogParachuteLanding", new ParachuteLandingEventHandler());
        telemetryEventHandlerMap.put("LogGameStatePeriodic", new GameStatePeriodicEventHandler());

        GameData gameData = telemetryProcessor.process(new GZIPInputStream(new URL(telemetryUrl).openStream())).join();

        Map<String, SortedMap<String, String>> playerDataPoints = gameData.getPlayerDataPoints("1.0");

        return ResponseEntity.ok(predictionService.getPrediction(playerDataPoints));
    }

    @GetMapping("/train")
    ResponseEntity<TrainingResult> train() throws IOException{
        if(buildTrainingData){
            return ResponseEntity.ok(batchTelemetryProcessor.process());
        }
        return ResponseEntity.ok(new TrainingResult());
    }

}
