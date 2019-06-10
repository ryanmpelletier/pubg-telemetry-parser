package com.github.ryanp102694.pubgtelemetryparser.data.event;

import org.json.JSONObject;

public interface FromJsonObject<T> {

    T fromJSONObject(JSONObject jsonObject);
}
