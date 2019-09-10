package com.github.ryanp102694.pubgtelemetryparser.data.model;

public class PlayerPrediction {

    private String name;
    private Boolean prediction;
    private Boolean correct;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPrediction() {
        return prediction;
    }

    public void setPrediction(Boolean prediction) {
        this.prediction = prediction;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }
}
