package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import org.json.JSONObject;

import org.springframework.stereotype.Component;

@Component("LogParachuteLanding")
public class ParachuteLandingEventHandler implements TelemetryEventHandler {

    @Override
    public void handle(JSONObject event, GameData gameData) {

    }

}
