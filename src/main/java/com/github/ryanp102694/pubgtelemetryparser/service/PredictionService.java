package com.github.ryanp102694.pubgtelemetryparser.service;

import com.github.ryanp102694.pubgtelemetryparser.data.model.PlayerPrediction;
import com.github.ryanp102694.pubgtelemetryparser.data.model.Prediction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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

        Map<String, List<PlayerPrediction>> playerPredictions  = new HashMap<>();

        for(int i = 0; i < jsonArray.length(); i++){

            JSONObject mlPrediction = jsonArray.getJSONObject(i);

            for(String playerNameWithPhase : mlPrediction.keySet()){ //should only be one in here, until I do refactoring
                String playerName = String.join("_",
                        Arrays.copyOfRange(playerNameWithPhase.split("_"),0,
                                (int) playerNameWithPhase.chars().filter(ch -> ch == '_').count()));

                if(playerPredictions.get(playerName) == null){
                    playerPredictions.put(playerName, new ArrayList<>());
                }

                PlayerPrediction playerPrediction = new PlayerPrediction();
                playerPrediction.setPrediction("alive".equals(mlPrediction.get(playerNameWithPhase)));
                playerPrediction.setGamePhase(playerNameWithPhase.split("_")[(int) playerNameWithPhase.chars().filter(ch -> ch == '_').count()]);
                //prediction was correct if I said they were alive and they are, OR I said they were dead and they are
                playerPrediction.setCorrect(playerDataPoints.get(playerNameWithPhase).get("alive").equals("1") && playerPrediction.getPrediction() ||
                        playerDataPoints.get(playerNameWithPhase).get("alive").equals("0") && !playerPrediction.getPrediction());
                playerPredictions.get(playerName).add(playerPrediction);
            }
        }

        prediction.setCorrect(playerPredictions.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(PlayerPrediction::getCorrect).count());

        prediction.setIncorrect(
                playerPredictions.values().stream().flatMap(Collection::stream).count() - prediction.getCorrect());

        prediction.setPlayerPredictions(playerPredictions);
        return prediction;
    }
}
