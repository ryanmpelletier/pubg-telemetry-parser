package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import org.json.JSONObject;

/**
 * Implementations should be stateless.
 */
public interface TelemetryEventHandler {

    void handle(JSONObject event, GameData gameData);

}
