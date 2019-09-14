package com.github.ryanp102694.pubgtelemetryparser.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.SortedMap;

/*

Build something like this to send to the TensorFlow server

{
    "features":{
        "supernewb":[1, 0, 0, 0, 0, 0, 1, 1],
        "IStink":[1, 1, 1, 1, 1, 1, 1, 1],
        "LilProtein23":[1, 1, 1, 1, 1, 1, 1, 1]
    }
}

 */
@Component
public class PredictionRequestWriter {


    public String getPredictionServerQuery(Map<String, SortedMap<String, String>> playerDataPoints){
        StringBuilder predictionStringBuilder = new StringBuilder("{\"features\":{");

        for(String key : playerDataPoints.keySet()) {
            if(key != null) {

                predictionStringBuilder.append("\"").append(key).append("\":[");

                SortedMap<String, String> dataPoints = playerDataPoints.get(key);
                for(String dataPoint : dataPoints.keySet()){
                    if(!"alive".equals(dataPoint)){
                        predictionStringBuilder.append(dataPoints.get(dataPoint)).append(",");
                    }
                }
                predictionStringBuilder.setLength(predictionStringBuilder.length() - 1);
                predictionStringBuilder.append("],");
            }
        }
        predictionStringBuilder.setLength(predictionStringBuilder.length() - 1);
        predictionStringBuilder.append("}}");
        return predictionStringBuilder.toString();
    }
}
