package com.github.ryanp102694.pubgtelemetryparser.data;

import com.github.ryanp102694.pubgtelemetryparser.data.event.Location;
import com.github.ryanp102694.pubgtelemetryparser.data.event.Player;

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

    public Map<String, Map<String, String>> getPlayerDataPoints(String gamePhase){
        Map<String, Map<String, String>> returnMap = new HashMap<>();
        Map<String, PlayerState> startPhaseStates = getStatesByPhase(gamePhase);
        Map<String, PlayerState> nextPhaseStates = getStatesByPhase(String.valueOf(Double.parseDouble(gamePhase) + 1.0));

        //for each player in this list, I will build an entry in the map
        for(String playerName : startPhaseStates.keySet()){

            PlayerState startPlayerState = startPhaseStates.get(playerName);


            Map<String, String> statsMap = new HashMap<>();

            //if we don't have an entry in the next state, they dead
            statsMap.put("alive", nextPhaseStates.get(playerName) == null ? "0" : "1");
            statsMap.put("numAliveTeamMembers", String.valueOf(calculateAliveTeammates(startPlayerState.getPlayer().getTeamId(), startPlayerState.getPlayer().getName(), gamePhase)));
            statsMap.put("nearestTeamMember", calculateNearestTeamMember(startPlayerState.getPlayer(), gamePhase));

            returnMap.put(startPlayerState.getPlayer().getName(), statsMap);
        }


        return returnMap;
    }

    private String calculateNearestTeamMember(Player player, String gamePhase){
        Map<String, PlayerState> teamMemberStates = getTeamMemberStatesByGamePhase(player, gamePhase);

        Location playerLocation = player.getLocation();

        //~500 meters, your teammate will likely not be much help to you this far away
        Double nearestNeighbor = 500000.0;

        for(PlayerState teamMemberState : teamMemberStates.values()){
            nearestNeighbor = Math.min(nearestNeighbor, playerLocation.distanceBetween(teamMemberState.getPlayer().getLocation()));
        }

        return String.valueOf(nearestNeighbor);
    }

    private String calculateAliveTeammates(int teamNumber, String playerName, String isGame){
        Set<String> teamMembers = this.teamData.get(teamNumber);
        int aliveTeammates = 0;
        for(String teamMember : teamMembers){
            if(getStateByPhase(teamMember, isGame) != null && !teamMember.equals(playerName)){
                aliveTeammates++;
            }
        }
        return String.valueOf(aliveTeammates);
    }

    private Map<String, PlayerState> getTeamMemberStatesByGamePhase(Player player, String gamePhase){

        Map<String, PlayerState> playerStateMap = new HashMap<>();
        Set<String> teamMembers = this.teamData.get(player.getTeamId());
        teamMembers.remove(player.getName());

        for(String teamMemberName : teamMembers){

            playerStateMap.put(teamMemberName, null);

            List<PlayerState> playerStates = this.playerStateMap.get(teamMemberName);
            for(PlayerState playerState : playerStates){
                if(playerState.getGamePhase().equals(gamePhase)){
                    playerStateMap.put(teamMemberName, playerState);
                    break;
                }
            }
        }
        return playerStateMap;
    }

    public PlayerState getStateByPhase(String playerName, String gamePhase){
        for(PlayerState playerState : this.getPlayerStateMap().get(playerName)){
            if(playerState.getGamePhase().equals(gamePhase)){
                return playerState;
            }
        }
        return null;
    }


    public Map<String,PlayerState> getStatesByPhase(String gamePhase){
        Map<String, PlayerState> playerStates = new HashMap<>();

        for(String playerName : this.getPlayerStateMap().keySet()){
            for(PlayerState playerState : this.getPlayerStateMap().get(playerName)){
                if(playerState.getGamePhase().equals(gamePhase)){
                    playerStates.put(playerName, playerState);
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
