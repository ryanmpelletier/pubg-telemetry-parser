package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.PlayerState;
import com.github.ryanp102694.pubgtelemetryparser.data.event.Player;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayerPositionEventHandler implements TelemetryEventHandler {

    @Override
    public void handle(JSONObject event, GameData gameData) {

        Player player = new Player().fromJSONObject(event.getJSONObject("character"));

        if(gameData.getPlayerStateMap() == null){
            gameData.setPlayerStateMap(new HashMap<>());
        }

        if(gameData.getPlayerStateMap().get(player.getName()) == null){
            gameData.getPlayerStateMap().put(player.getName(), new ArrayList<>());
        }

        PlayerState playerState = new PlayerState();
        playerState.setPlayer(player);
        playerState.setTotalKills(playerState.getTotalKills());
        playerState.setNumAlivePlayers(event.getInt("numAlivePlayers"));
        playerState.setElapsedTime(event.getInt("elapsedTime"));
        playerState.setGamePhase(String.valueOf(event.getJSONObject("common").getDouble("isGame")));
        playerState.setTime(Instant.parse(event.getString("_D")));


        gameData.getPlayerStateMap().get(player.getName()).add(playerState);
    }
}
