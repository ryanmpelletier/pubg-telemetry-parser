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

    //if there are more than this many enemies in a zone, we'll just record 15, which will make the data easier to normalize
    static int ENEMY_NUMBER_CAP = 15;
    static double NEAREST_TEAM_MEMBER_CAP = 50000.0;
    static double NEAREST_ENEMY_CAP = 50000.0;
    static double DISTANCE_TO_SAFE_ZONE_CAP = 250000.0;
    static double EIGHT_KM_MAP_SIZE = 816000.0;


    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getMatchUUID(){
        String[] idParts = gameId.split("\\.");
        return idParts[idParts.length - 1];
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

    public Map<String, SortedMap<String, String>> getPlayerDataPoints(String gamePhase){
        Map<String, SortedMap<String, String>> returnMap = new HashMap<>();

        //it is not guaranteed a particular game will progress through all phases, if it doesn't no further processing
        if(getGameStateByPhase(gamePhase) == null){
            return returnMap;
        }

        Map<String, PlayerState> startPhaseStates = getStatesByPhase(gamePhase);
        Map<String, PlayerState> nextPhaseStates = getStatesByPhase(String.valueOf(Double.parseDouble(gamePhase) + 1.0));

        //for each player in this list, I will build an entry in the map
        for(String playerName : startPhaseStates.keySet()){

            PlayerState startPlayerState = startPhaseStates.get(playerName);


            SortedMap<String, String> statsMap = new TreeMap<>();

            //if we don't have an entry in the next state, they dead
            statsMap.put("alive", nextPhaseStates.get(playerName) == null ? "0" : "1");

            //will return number between 0 and NEAREST_ENEMY_CAP
            statsMap.put("closestEnemyDistance", calculateClosestEnemyDistance(startPlayerState.getPlayer(), gamePhase, new RangeMapper(0.0, NEAREST_ENEMY_CAP)));


            //number between 0 and DISTANCE_TO_SAFE_ZONE_CAP
            statsMap.put("distanceToSafeZone", calculateDistanceToSafeZone(startPlayerState.getPlayer(), gamePhase, new RangeMapper(0.0, DISTANCE_TO_SAFE_ZONE_CAP)));

            RangeMapper enemyRangeMapper = new RangeMapper(0.0, ENEMY_NUMBER_CAP);

            //will return number between 0 and ENEMY_NUMBER_CAP
            statsMap.put("enemyCountZeroToTwentyFive", calculateEnemiesWithinDistance(startPlayerState.getPlayer(), gamePhase, 0.0, 25.0, enemyRangeMapper));
            statsMap.put("enemyCountTwentyFiveToFifty", calculateEnemiesWithinDistance(startPlayerState.getPlayer(), gamePhase, 25.0, 50.0, enemyRangeMapper));
            statsMap.put("enemyCountFiftyToOneHundred", calculateEnemiesWithinDistance(startPlayerState.getPlayer(), gamePhase, 50.0, 100.0, enemyRangeMapper));
            statsMap.put("enemyCountOneHundredToTwoFifty", calculateEnemiesWithinDistance(startPlayerState.getPlayer(), gamePhase, 100.0, 250.0, enemyRangeMapper));

            //which game phase we are in, we'll do up to 7.0 for now
            statsMap.put("gamePhase1", "1.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase2", "2.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase3", "3.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase4", "4.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase5", "5.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase6", "6.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase7", "7.0".equals(gamePhase) ? "1.0" : "0.0");

            //return number between 0 and NEAREST_TEAM_MEMBER_CAP
            statsMap.put("nearestTeamMember", calculateNearestTeamMember(startPlayerState.getPlayer(), gamePhase, new RangeMapper(0.0, NEAREST_TEAM_MEMBER_CAP)));

            //return between 0 and 3
            statsMap.put("numAliveTeamMembers", calculateAliveTeammates(startPlayerState.getPlayer().getTeamId(), startPlayerState.getPlayer().getName(), gamePhase, new RangeMapper(0.0, 3.0)));

            statsMap.put("xPosition", calculatePositionValue(startPlayerState.getPlayer().getLocation().getX(), new RangeMapper(0.0, EIGHT_KM_MAP_SIZE)));
            statsMap.put("yPosition", calculatePositionValue(startPlayerState.getPlayer().getLocation().getY(), new RangeMapper(0.0, EIGHT_KM_MAP_SIZE)));

            returnMap.put(startPlayerState.getPlayer().getName() + "_" + gamePhase, statsMap);
        }

        return returnMap;
    }

    private String calculateEnemiesWithinDistance(Player player, String gamePhase, Double minRange, Double maxRange, RangeMapper rangeMapper){
        Map<String, PlayerState> enemyPlayerStates = getEnemyStatesByGamePhase(player, gamePhase);

        Location playerLocation = player.getLocation();
        Double numberOfEnemies = 0.0;

        for(PlayerState enemyPlayerState : enemyPlayerStates.values()){
            Double distance = playerLocation.distanceBetween(enemyPlayerState.getPlayer().getLocation());
            if(distance >= (minRange * 100) && distance < (maxRange * 100)){
                numberOfEnemies++;
            }
        }
        return String.format("%.2f", rangeMapper.apply(Math.min(numberOfEnemies, ENEMY_NUMBER_CAP)));
    }

    private String calculateClosestEnemyDistance(Player player, String gamePhase, RangeMapper rangeMapper){
        Map<String, PlayerState> enemyPlayerStates = getEnemyStatesByGamePhase(player, gamePhase);

        Location playerLocation = player.getLocation();
        //enemies aren't realistically dangerous from 500 meters away
        Double closestEnemyDistance = NEAREST_ENEMY_CAP;

        for(PlayerState enemyPlayerState : enemyPlayerStates.values()){
            closestEnemyDistance = Math.min(closestEnemyDistance, playerLocation.distanceBetween(enemyPlayerState.getPlayer().getLocation()));
        }
        return String.format("%.2f", rangeMapper.apply(closestEnemyDistance));
    }

    private String calculateSafeZoneRadius(String gamePhase){
        return String.format("%.2f", getGameStateByPhase(gamePhase).getSafetyZoneRadius());
    }

    private String calculateDistanceToSafeZone(Player player, String gamePhase, RangeMapper rangeMapper){
        GameState gameState = getGameStateByPhase(gamePhase);

        Location safetyZonePosition = gameState.getSafetyZonePosition();
        Location playerLocation = player.getLocation();

        Double distance = Math.max(safetyZonePosition.distanceBetween(playerLocation) - gameState.getSafetyZoneRadius(), 0.0);
        Double boundedDistance = Math.min(DISTANCE_TO_SAFE_ZONE_CAP, distance);
        return String.format("%.2f", rangeMapper.apply((boundedDistance)));
    }

    private String calculatePositionValue(Double positionValue, RangeMapper rangeMapper){
        return String.format("%.2f", rangeMapper.apply(positionValue));
    }

    private String calculateNearestTeamMember(Player player, String gamePhase, RangeMapper rangeMapper){
        Map<String, PlayerState> teamMemberStates = getTeamMemberStatesByGamePhase(player, gamePhase);

        Location playerLocation = player.getLocation();

        //~500 meters, your teammate will likely not be much help to you this far away
        Double nearestNeighbor = NEAREST_TEAM_MEMBER_CAP;

        for(PlayerState teamMemberState : teamMemberStates.values()){
            nearestNeighbor = Math.min(nearestNeighbor, playerLocation.distanceBetween(teamMemberState.getPlayer().getLocation()));
        }

        return String.format("%.2f", rangeMapper.apply(nearestNeighbor));
    }

    private String calculateAliveTeammates(int teamNumber, String playerName, String isGame, RangeMapper rangeMapper){
        Set<String> teamMembers = this.teamData.get(teamNumber);
        Double aliveTeammates = 0.0;
        for(String teamMember : teamMembers){
            if(getStateByPhase(teamMember, isGame) != null && !teamMember.equals(playerName)){
                aliveTeammates++;
            }
        }
        return String.format("%.2f", rangeMapper.apply(aliveTeammates));
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
