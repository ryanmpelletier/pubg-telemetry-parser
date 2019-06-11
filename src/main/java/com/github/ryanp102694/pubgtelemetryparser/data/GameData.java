package com.github.ryanp102694.pubgtelemetryparser.data;

import java.time.Instant;
import java.util.*;

/**
 * Will store all game info as game progresses.
 * Should be able to query this object with a time and get the state of all players of the game, dead or alive.
 *
 * A different GameData object will be created for each thread which is parsing telemetry
 */
public class GameData {

    private String gameId;
    private Instant startTime;

    /**
     * This will hold information about what is on the map. Should know where red/blue, white zone are.
     * Should know where packages are and what time they landed.
     */
    private List<GameState> gameStates;
    private Map<Integer, Set<String>> teamData;
    private Map<String, List<PlayerState>> playerStateMap;


    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public List<GameState> getGameStates() {
        return gameStates;
    }

    public void setGameStates(List<GameState> gameStates) {
        this.gameStates = gameStates;
    }

    public Map<Integer, Set<String>> getTeamData() {
        return teamData;
    }

    public void setTeamData(Map<Integer, Set<String>> teamData) {
        this.teamData = teamData;
    }

    public Map<String, List<PlayerState>> getPlayerStateMap() {
        return playerStateMap;
    }

    public void setPlayerStateMap(Map<String, List<PlayerState>> playerStateMap) {
        this.playerStateMap = playerStateMap;
    }

//    public Map<String, Map<String, String>> getPlayerDataPoints(String isGame){
//        Map<String, Map<String, String>> returnMap = new HashMap<>();
//        List<PlayerState> startPhaseStates = getStatesByPhase(isGame);
//        List<PlayerState> nextPhaseStates = getStatesByPhase(String.valueOf(Double.parseDouble(isGame) + 1.0));
//
//        for(PlayerState playerState : startPhaseStates){
//
//        }
//
//
//        return returnMap;
//    }


    public List<PlayerState> getStatesByPhase(String isGame){
        List<PlayerState> playerStates = new ArrayList<>();

        for(String playerName : this.getPlayerStateMap().keySet()){
            for(PlayerState playerState : this.getPlayerStateMap().get(playerName)){
                if(playerState.getIsGame().equals(isGame)){
                    playerStates.add(playerState);
                    break;
                }
            }
        }
        return playerStates;
    }

    public PlayerState getPlayerState(String playerName, Instant time) {
         List<PlayerState> playerStates = this.getPlayerStateMap().get(playerName);

         //return the player state just before the requested time
         for(int i = 0; i < playerStates.size(); i++){
             if(playerStates.get(i).getTime().isAfter(this.startTime)){
                 return playerStates.get(i);
             }
         }
         //get the last recorded player state if one is not found
         return playerStates.get(playerStates.size() - 1);
    }

}
