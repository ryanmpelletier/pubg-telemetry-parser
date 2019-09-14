package com.github.ryanp102694.pubgtelemetryparser.data.model;

import java.util.List;
import java.util.Map;

public class Prediction {

    private long correct;
    private long incorrect;
    private Map<String, List<PlayerPrediction>> playerPredictions;


    public long getCorrect() {
        return correct;
    }

    public void setCorrect(long correct) {
        this.correct = correct;
    }

    public void setIncorrect(long incorrect) {
        this.incorrect = incorrect;
    }

    public void setPlayerPredictions(Map<String, List<PlayerPrediction>> playerPredictions) {
        this.playerPredictions = playerPredictions;
    }
}
