package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import org.json.JSONObject;

import org.springframework.stereotype.Component;

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


    /**
     * I don't think I want this.
     */
    @Override
    public void handle(JSONObject event, GameData gameData) {

    }
}
