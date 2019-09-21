package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.PlayerState;
import com.github.ryanp102694.pubgtelemetryparser.data.event.Player;
import org.json.JSONObject;

import java.time.Instant;
import java.util.List;


/**
 * PlayerKillEvent
 * "attackId":                   int,
 * "killer":                     {Character},
 * "victim":                     {Character},
 * "assistant":                  {Character},
 * "dBNOId":                     int
 * "damageTypeCategory":         string,
 * "damageCauserName":           string,
 * "damageCauserAdditionalInfo": [string],
 * "damageReason":               string,
 * "distance":                   number,
 * "victimGameResult":           {GameResult}
 *
 * GameResult
 * {
 *   "rank":       int,
 *   "gameResult": string,
 *   "teamId":     int,
 *   "stats":      {Stats},
 *   "accountId":  string
 * }
 *
 * Stats
 * {
 *   "killCount":           int,
 *   "distanceOnFoot":      number,
 *   "distanceOnSwim":      number,
 *   "distanceOnVehicle":   number,
 *   "distanceOnParachute": number,
 *   "distanceOnFreefall":  number
 * }
 */
public class PlayerKillEventHandler implements TelemetryEventHandler {

    @Override
    public void handle(JSONObject event, GameData gameData) {
        if(!event.isNull("killer")){
            JSONObject killer = event.getJSONObject("killer");
            gameData.getPlayerKillsMap().computeIfAbsent(killer.getString("name"), playerName  -> 0.0);
            gameData.getPlayerKillsMap().put(killer.getString("name"), gameData.getPlayerKillsMap().get(killer.getString("name")) + 1.0);
        }
    }
}
