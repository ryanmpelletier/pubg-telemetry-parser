package com.github.ryanp102694.pubgtelemetryparser.event;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public interface EventHandler {

    void handle(JSONObject event, GameData gameData);

}
