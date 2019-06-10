package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import org.json.JSONObject;

public class MatchDefinitionEventHandler implements TelemetryEventHandler {

    @Override
    public void handle(JSONObject event, GameData gameData) {
        gameData.setGameId(event.getString("MatchId"));
    }

}
