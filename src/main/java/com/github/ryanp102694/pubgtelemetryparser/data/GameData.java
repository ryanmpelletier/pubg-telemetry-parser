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
            statsMap.put("distanceToSafeZone", calculateDistanceToSafeZone(startPlayerState.getPlayer(), gamePhase));
            statsMap.put("safeZoneRadius", calculateSafeZoneRadius(gamePhase));
            statsMap.put("closestEnemyDistance", calculateClosestEnemyDistance(startPlayerState.getPlayer(), gamePhase));
            statsMap.put("enemyCountZeroToTwentyFive", calculateEnemiesWithinDistance(startPlayerState.getPlayer(), gamePhase, 0.0, 25.0));
            statsMap.put("enemyCountTwentyFiveToFifty", calculateEnemiesWithinDistance(startPlayerState.getPlayer(), gamePhase, 25.0, 50.0));
            statsMap.put("enemyCountFiftyToOneHundred", calculateEnemiesWithinDistance(startPlayerState.getPlayer(), gamePhase, 50.0, 100.0));
            statsMap.put("enemyCountOneHundredToTwoFifty", calculateEnemiesWithinDistance(startPlayerState.getPlayer(), gamePhase, 100.0, 250.0));
            returnMap.put(startPlayerState.getPlayer().getName(), statsMap);
        }

        return returnMap;
    }

    private String calculateEnemiesWithinDistance(Player player, String gamePhase, Double minRange, Double maxRange){
        Map<String, PlayerState> enemyPlayerStates = getEnemyStatesByGamePhase(player, gamePhase);

        Location playerLocation = player.getLocation();
        //enemies aren't realistically dangerous from 500 meters away
        Integer numberOfEnemies = 0;

        for(PlayerState enemyPlayerState : enemyPlayerStates.values()){
            Double distance = playerLocation.distanceBetween(enemyPlayerState.getPlayer().getLocation());
            if(distance >= (minRange * 100) && distance < (maxRange * 100)){
                numberOfEnemies++;
            }
        }
        return String.valueOf(numberOfEnemies);
    }

    private String calculateClosestEnemyDistance(Player player, String gamePhase){
        Map<String, PlayerState> enemyPlayerStates = getEnemyStatesByGamePhase(player, gamePhase);

        Location playerLocation = player.getLocation();
        //enemies aren't realistically dangerous from 500 meters away
        Double closestEnemyDistance = 50000.0;

        for(PlayerState enemyPlayerState : enemyPlayerStates.values()){
            closestEnemyDistance = Math.min(closestEnemyDistance, playerLocation.distanceBetween(enemyPlayerState.getPlayer().getLocation()));
        }
        return String.valueOf(closestEnemyDistance);
    }

    private String calculateSafeZoneRadius(String gamePhase){
        return String.valueOf(getGameStateByPhase(gamePhase).getSafetyZoneRadius());
    }

    private String calculateDistanceToSafeZone(Player player, String gamePhase){
        GameState gameState = getGameStateByPhase(gamePhase);
        return String.valueOf(gameState.getSafetyZonePosition().distanceBetween(player.getLocation()));
    }

    private String calculateNearestTeamMember(Player player, String gamePhase){
        Map<String, PlayerState> teamMemberStates = getTeamMemberStatesByGamePhase(player, gamePhase);

        Location playerLocation = player.getLocation();

        //~500 meters, your teammate will likely not be much help to you this far away
        Double nearestNeighbor = 50000.0;

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

    private GameState getGameStateByPhase(String gamePhase){
        for(GameState gameState : this.gameStates){
            if(gamePhase.equals(gameState.getGamePhase())){
                return gameState;
            }
        }
        return null;
    }

    private Map<String, PlayerState> getEnemyStatesByGamePhase(Player player, String gamePhase){
        Map<String, PlayerState> enemyPlayerStatesMap = new HashMap<>();
        Set<String> teamMembers = this.teamData.get(player.getTeamId());

        for(String playerName : this.getPlayerStateMap().keySet()){
            if(!teamMembers.contains(playerName)){
                for(PlayerState playerState : this.getPlayerStateMap().get(playerName)){
                    if(playerState.getGamePhase().equals(gamePhase)){
                        enemyPlayerStatesMap.put(playerName, playerState);
                        break;
                    }
                }
            }
        }
        return enemyPlayerStatesMap;
    }

    private Map<String, PlayerState> getTeamMemberStatesByGamePhase(Player player, String gamePhase){

        Map<String, PlayerState> playerStateMap = new HashMap<>();
        Set<String> teamMembers = new HashSet<>(this.teamData.get(player.getTeamId()));
        teamMembers.remove(player.getName());

        for(String teamMemberName : teamMembers){

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

    private PlayerState getStateByPhase(String playerName, String gamePhase){
        for(PlayerState playerState : this.getPlayerStateMap().get(playerName)){
            if(playerState.getGamePhase().equals(gamePhase)){
                return playerState;
            }
        }
        return null;
    }


    private Map<String,PlayerState> getStatesByPhase(String gamePhase){
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

    private PlayerState getPlayerState(String playerName, Instant time) {
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
