package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.GameState;
import com.github.ryanp102694.pubgtelemetryparser.data.event.Location;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

/**
 * "gameState": {
 *   "elapsedTime":              int,
 *   "numAliveTeams":            int,
 *   "numJoinPlayers":           int,
 *   "numStartPlayers":          int,
 *   "numAlivePlayers":          int,
 *   "safetyZonePosition":       {Location},
 *   "safetyZoneRadius":         number,
 *   "poisonGasWarningPosition": {Location},
 *   "poisonGasWarningRadius":   number,
 *   "redZonePosition":          {Location},
 *   "redZoneRadius":            number
 * }
 */
@Component("LogGameStatePeriodic")
public class GameStatePeriodicEventHandler implements TelemetryEventHandler {

    @Override
    public void handle(JSONObject event, GameData gameData) {

        if(null == gameData.getGameStates()){
            gameData.setGameStates(new ArrayList<>());
        }

        GameState gameState = new GameState();

        gameState.setGamePhase(String.valueOf(event.getJSONObject("common").getDouble("isGame")));
        gameState.setTime(Instant.parse(event.getString("_D")));

        event = event.getJSONObject("gameState");
        gameState.setElapsedTime(event.getInt("elapsedTime"));
        gameState.setNumAliveTeams(event.getInt("numAliveTeams"));
        gameState.setNumJoinPlayers(event.getInt("numJoinPlayers"));
        gameState.setNumStartPlayers(event.getInt("numStartPlayers"));
        gameState.setNumAlivePlayers(event.getInt("numAlivePlayers"));
        gameState.setSafetyZoneRadius(event.getDouble("safetyZoneRadius"));
        gameState.setSafetyZonePosition(new Location().fromJSONObject(event.getJSONObject("safetyZonePosition")));
        gameState.setRedZoneRadius(event.getDouble("redZoneRadius"));
        gameState.setRedZonePosition(new Location().fromJSONObject(event.getJSONObject("redZonePosition")));
        gameState.setPoisonGasWarningRadius(event.getDouble("poisonGasWarningRadius"));
        gameState.setPoisonGasWarningPosition(new Location().fromJSONObject(event.getJSONObject("poisonGasWarningPosition")));
        gameState.setElapsedTime(event.getInt("elapsedTime"));

        gameData.getGameStates().add(gameState);
    }


}
