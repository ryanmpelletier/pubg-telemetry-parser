package com.github.ryanp102694.pubgtelemetryparser;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class GameDataWrite {


    public GameDataWrite(){}

    public void writeGameDataPoints(GameData gameData) throws IOException {

        String trainingDataFileName = gameData.getGameId() + "_training.csv";
        String trainingLabelFileName = gameData.getGameId() + "_labels.csv";

        Map<String, Map<String, String>> playerDataPoints = gameData.getPlayerDataPoints("1.0");

        BufferedWriter trainingDataWriter = new BufferedWriter(new FileWriter(trainingDataFileName));
        BufferedWriter trainingLabelWriter = new BufferedWriter(new FileWriter(trainingLabelFileName));


        Collection<Map<String, String>> dataPoints = playerDataPoints.values();
        for(Map<String, String> dataPoint : dataPoints){
            for(String key : dataPoint.keySet()){
                if("alive".equals(key)){
                    trainingLabelWriter.write(dataPoint.get(key) + "\n");
                }else{
                    trainingDataWriter.write(dataPoint.get(key) + ",");
                }
            }
            trainingDataWriter.write("\n");
        }




        trainingDataWriter.close();
        trainingLabelWriter.close();
    }


}
