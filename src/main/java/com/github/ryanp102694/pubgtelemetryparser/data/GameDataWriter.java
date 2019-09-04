package com.github.ryanp102694.pubgtelemetryparser.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


//I want this to be spring managed and have @Async methods
public class GameDataWriter {

    private final static Logger log = LoggerFactory.getLogger(GameDataWriter.class);

    //I don't want this in here, this locks it to writing to a particular directory
    private String outputDir = ".";

    public GameDataWriter(String outputDir){
        this.outputDir = outputDir;
    }

    public void writeGameDataPoints(GameData gameData) throws IOException {

        String[] idParts = gameData.getGameId().split("\\.");
        String matchUUID = idParts[idParts.length - 1];

        //this method should be given this and not call it
        Map<String, SortedMap<String, String>> playerDataPoints = gameData.getPlayerDataPoints("1.0");

        BufferedWriter trainingDataWriter = new BufferedWriter(new FileWriter(outputDir + "/" + matchUUID + ".training.csv"));
        BufferedWriter trainingLabelWriter = new BufferedWriter(new FileWriter(outputDir + "/" + matchUUID + ".labels.csv"));

        List<SortedMap<String, String>> dataPointsList = new ArrayList<>(playerDataPoints.values());

        //these strings should be built at the same time
        trainingDataWriter.write(getTrainingDataString(dataPointsList));
        trainingLabelWriter.write(getLabelsString(dataPointsList));

        trainingDataWriter.close();
        trainingLabelWriter.close();
    }

    //I want a method very similar to this for getting my prediction request JSON
    private String getTrainingDataString(List<SortedMap<String, String>> dataPointsList){
        StringBuilder trainingDataBuilder = new StringBuilder();

        for(int i = 0; i < dataPointsList.size(); i++){

            SortedMap<String, String> dataPoints = dataPointsList.get(i);

            for(String key : dataPoints.keySet()){
                if(!"alive".equals(key)){
                    trainingDataBuilder.append(dataPoints.get(key)).append(",");
                }
            }
            trainingDataBuilder.setLength(trainingDataBuilder.length() - 1);
            //add a newline if there are more datapoints
            if(i != dataPointsList.size() - 1){
                trainingDataBuilder.append("\n");
            }
        }
        return trainingDataBuilder.toString();
    }

    private String getLabelsString(List<SortedMap<String, String>> dataPointsList){
        StringBuilder labelBuilder = new StringBuilder();

        for(int i = 0; i < dataPointsList.size(); i++){

            SortedMap<String, String> dataPoints = dataPointsList.get(i);

            for(String key : dataPoints.keySet()){
                if("alive".equals(key)){
                    String otherString = "";
                    if("1".equals(dataPoints.get(key))){
                        otherString = "0";
                    }else{
                        otherString = "1";
                    }
                    labelBuilder.append(dataPoints.get(key)).append(",").append(otherString);
                }
            }
            //add a newline if there are more datapoints
            if(i != dataPointsList.size() - 1){
                labelBuilder.append("\n");
            }
        }

        return labelBuilder.toString();
    }


    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
