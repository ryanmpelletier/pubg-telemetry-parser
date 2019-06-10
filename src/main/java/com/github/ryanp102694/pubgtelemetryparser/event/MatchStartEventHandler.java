package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MatchStartEventHandler implements TelemetryEventHandler {

    @Override
    public void handle(JSONObject event, GameData gameData) {

        gameData.setTeamData(buildTeams(event));
    }

    public Map<Integer, Set<String>> buildTeams(JSONObject matchStartEvent){


        if(!"LogMatchStart".equals(matchStartEvent.getString("_T"))){
            throw new RuntimeException("Must build teams from LogMatchStart");
        }

        Map<Integer, Set<String>> teamMap = new HashMap<>();

        JSONArray charactersArray = matchStartEvent.getJSONArray("characters");

        for(int i = 0; i < charactersArray.length(); i++){
            JSONObject character = charactersArray.getJSONObject(i);
            if(!teamMap.containsKey(character.getInt("teamId"))){
                teamMap.put(character.getInt("teamId"), new HashSet<>());
            }
            teamMap.get(character.getInt("teamId")).add(character.getString("name"));

        }
        return teamMap;
    }

}
