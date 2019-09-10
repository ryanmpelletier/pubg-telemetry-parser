package com.github.ryanp102694.pubgtelemetryparser.data.model;

import java.util.List;

public class Prediction {

    private long correct;
    private long incorrect;
    private List<PlayerPrediction> playerPredictions;


    public long getCorrect() {
        return correct;
    }

    public void setCorrect(long correct) {
        this.correct = correct;
    }

    public long getIncorrect() {
        return incorrect;
    }

    public void setIncorrect(long incorrect) {
        this.incorrect = incorrect;
    }

    public List<PlayerPrediction> getPlayerPredictions() {
        return playerPredictions;
    }

    public void setPlayerPredictions(List<PlayerPrediction> playerPredictions) {
        this.playerPredictions = playerPredictions;
    }
}
