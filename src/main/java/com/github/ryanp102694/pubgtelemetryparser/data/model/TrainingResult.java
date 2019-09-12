package com.github.ryanp102694.pubgtelemetryparser.data.model;

public class TrainingResult {
    long timeElapsed;
    long filesProcessed;
    String trainingDataDirectory;

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public long getFilesProcessed() {
        return filesProcessed;
    }

    public void setFilesProcessed(long filesProcessed) {
        this.filesProcessed = filesProcessed;
    }

    public String getTrainingDataDirectory() {
        return trainingDataDirectory;
    }

    public void setTrainingDataDirectory(String trainingDataDirectory) {
        this.trainingDataDirectory = trainingDataDirectory;
    }
}
