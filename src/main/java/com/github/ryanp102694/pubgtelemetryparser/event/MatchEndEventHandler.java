package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.event.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//we don't actually need this, we can calculate it from the GameData
public class MatchEndEventHandler implements TelemetryEventHandler {

    @Override
    public void handle(JSONObject event, GameData gameData) {
        List<Player> winners = new ArrayList<>();

        JSONArray jsonArray = event.getJSONArray("characters");

        for(int i = 0; i < jsonArray.length(); i++){

            JSONObject characterObject = jsonArray.getJSONObject(i);
            //character became nested
            if(characterObject.keySet().contains("character")){
                characterObject = characterObject.getJSONObject("character");
            }


            winners.add(new Player().fromJSONObject(characterObject));
        }
        winners = winners.stream().filter(player -> {
            return player.getHealth() > 0.0;
        }).collect(Collectors.toList());

        gameData.setWinners(winners);
    }
}
