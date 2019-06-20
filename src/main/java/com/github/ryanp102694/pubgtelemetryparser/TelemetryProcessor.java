package com.github.ryanp102694.pubgtelemetryparser;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.data.GameDataWriter;
import com.github.ryanp102694.pubgtelemetryparser.event.TelemetryEventHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class TelemetryProcessor implements Runnable {

    private String telemetryFileName;
    private String outputDirectory = ".";
    private GameData gameData;
    Map<String, TelemetryEventHandler> telemetryEventHandlerMap;

    public TelemetryProcessor(){
        this.gameData = new GameData();
    }

    @Override
    public void run() {
        JSONArray telemetryEvents = null;

        try{
            String jsonString = new String(Files.readAllBytes(Paths.get(telemetryFileName)));
            telemetryEvents = new JSONArray(jsonString);
            for(int i = 0; i < telemetryEvents.length(); i++) {
                JSONObject telemetryEvent = telemetryEvents.getJSONObject(i);
                String eventType = telemetryEvent.getString("_T");

                if(telemetryEventHandlerMap.get(eventType) != null){
                    telemetryEventHandlerMap.get(eventType).handle(telemetryEvent, this.gameData);
                }
            }

            GameDataWriter gameDataWriter = new GameDataWriter();
            gameDataWriter.writeGameDataPoints(this.gameData);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public String getTelemetryFileName() {
        return telemetryFileName;
    }

    public void setTelemetryFileName(String telemetryFileName) {
        this.telemetryFileName = telemetryFileName;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Map<String, TelemetryEventHandler> getTelemetryEventHandlerMap() {
        return telemetryEventHandlerMap;
    }

    public void setTelemetryEventHandlerMap(Map<String, TelemetryEventHandler> telemetryEventHandlerMap) {
        this.telemetryEventHandlerMap = telemetryEventHandlerMap;
    }
}
