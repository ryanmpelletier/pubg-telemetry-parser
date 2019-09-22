package com.github.ryanp102694.pubgtelemetryparser.data.model;
import java.util.HashMap;
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

    public long getIncorrect() {
        return incorrect;
    }

    public void setIncorrect(long incorrect) {
        this.incorrect = incorrect;
    }

    public Map<String, List<PlayerPrediction>> getPlayerPredictions() {
        return playerPredictions;
    }

    public void setPlayerPredictions(Map<String, List<PlayerPrediction>> playerPredictions) {
        this.playerPredictions = playerPredictions;
    }

    public Map<String, String> getPhaseAccuracy(){

        Map<String, String> phaseAccurracyMap = new HashMap<>();
        Map<String, Integer> correctCountByPhase = new HashMap<>();
        Map<String, Integer> incorrectCountByPhase = new HashMap<>();

        for(String player: playerPredictions.keySet()){
            for(PlayerPrediction playerPrediction : playerPredictions.get(player)){
                String gamePhase = playerPrediction.getGamePhase();
                correctCountByPhase.computeIfAbsent(gamePhase, phase -> 0);
                incorrectCountByPhase.computeIfAbsent(gamePhase, phase -> 0);

                if(playerPrediction.getCorrect()){
                    correctCountByPhase.put(gamePhase, correctCountByPhase.get(gamePhase) + 1);
                }else{
                    incorrectCountByPhase.put(gamePhase, incorrectCountByPhase.get(gamePhase) + 1);
                }
                phaseAccurracyMap.computeIfAbsent(gamePhase, phase -> "0.0");
                phaseAccurracyMap.put(gamePhase, String.format("%.2f",(double) correctCountByPhase.get(gamePhase) /
                        (double) (correctCountByPhase.get(gamePhase) + incorrectCountByPhase.get(gamePhase))));
            }
        }


        return phaseAccurracyMap;
    }
}
