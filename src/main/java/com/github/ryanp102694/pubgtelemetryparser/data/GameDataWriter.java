package com.github.ryanp102694.pubgtelemetryparser.data;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GameDataWriter {

    private String outputDir = ".";


    public GameDataWriter(){}

    public GameDataWriter(String outputDir){
        this.outputDir = outputDir;
    }

    public void writeGameDataPoints(GameData gameData) throws IOException {

        String[] idParts = gameData.getGameId().split("\\.");
        String matchUUID = idParts[idParts.length - 1];

        Map<String, SortedMap<String, String>> playerDataPoints = gameData.getPlayerDataPoints("1.0");

        BufferedWriter trainingDataWriter = new BufferedWriter(new FileWriter(outputDir + "/" + matchUUID + ".training.csv"));
        BufferedWriter trainingLabelWriter = new BufferedWriter(new FileWriter(outputDir + "/" + matchUUID + ".labels.csv"));

        List<SortedMap<String, String>> dataPointsList = new ArrayList<>(playerDataPoints.values());

        StringBuilder labelBuilder = new StringBuilder();
        StringBuilder trainingDataBuilder = new StringBuilder();

        for(int i = 0; i < dataPointsList.size(); i++){

            SortedMap<String, String> dataPoints = dataPointsList.get(i);

            for(String key : dataPoints.keySet()){
                if("alive".equals(key)){
                    labelBuilder.append(dataPoints.get(key));
                }else{
                    trainingDataBuilder.append(dataPoints.get(key) + ",");
                }
            }
            trainingDataBuilder.setLength(trainingDataBuilder.length() - 1);

            //add a newline if there are more datapoints
            if(i != dataPointsList.size() - 1){
                labelBuilder.append("\n");
                trainingDataBuilder.append("\n");
            }
        }

        trainingDataWriter.write(trainingDataBuilder.toString());
        trainingLabelWriter.write(labelBuilder.toString());

        trainingDataWriter.close();
        trainingLabelWriter.close();
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
