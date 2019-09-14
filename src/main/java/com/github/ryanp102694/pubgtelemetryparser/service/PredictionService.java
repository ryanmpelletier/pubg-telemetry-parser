package com.github.ryanp102694.pubgtelemetryparser.service;

import com.github.ryanp102694.pubgtelemetryparser.data.model.PlayerPrediction;
import com.github.ryanp102694.pubgtelemetryparser.data.model.Prediction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@Component
public class PredictionService {

    PredictionClient predictionClient;
    PredictionRequestWriter predictionRequestWriter;

    public PredictionService(@Autowired PredictionClient predictionClient,
                             @Autowired PredictionRequestWriter predictionRequestWriter){
        this.predictionClient = predictionClient;
        this.predictionRequestWriter = predictionRequestWriter;
    }

    public String getPredictionServerQuery(Map<String, SortedMap<String, String>> playerDataPoints){
        return predictionRequestWriter.getPredictionServerQuery(playerDataPoints);
    }


    public Prediction getPrediction(Map<String, SortedMap<String, String>> playerDataPoints){
        return predictionFromJson(predictionClient.makePrediction(predictionRequestWriter.getPredictionServerQuery(playerDataPoints)), playerDataPoints);
    }


    private Prediction predictionFromJson(String jsonPrediction, Map<String, SortedMap<String, String>> playerDataPoints){
        JSONArray jsonArray = new JSONArray(jsonPrediction);

        //do stuff here to build prediction
        Prediction prediction = new Prediction();

        List<PlayerPrediction> playerPredictions = new ArrayList<>();

        for(int i = 0; i < jsonArray.length(); i++){

            JSONObject mlPrediction = jsonArray.getJSONObject(i);

            for(String playerName : mlPrediction.keySet()){ //should only be one in here, until I do refactoring
                PlayerPrediction playerPrediction = new PlayerPrediction();
                playerPrediction.setName(playerName);
                playerPrediction.setPrediction("alive".equals(mlPrediction.get(playerName)));
                //prediction was correct if I said they were alive and they are, OR I said they were dead and they are
                playerPrediction.setCorrect(playerDataPoints.get(playerName).get("alive").equals("1") && playerPrediction.getPrediction() ||
                        playerDataPoints.get(playerName).get("alive").equals("0") && !playerPrediction.getPrediction());
                playerPredictions.add(playerPrediction);
            }
        }

        prediction.setCorrect(playerPredictions.stream().filter(PlayerPrediction::getCorrect).count());
        prediction.setIncorrect(playerPredictions.size() - prediction.getCorrect());

        prediction.setPlayerPredictions(playerPredictions);
        return prediction;
    }
}
