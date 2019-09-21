package com.github.ryanp102694.pubgtelemetryparser.data;

import com.github.ryanp102694.pubgtelemetryparser.data.event.Location;
import com.github.ryanp102694.pubgtelemetryparser.data.event.Player;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Will store all game info as game progresses.
 * Should be able to query this object with a time and get the state of all players of the game, dead or alive.
 *
 * A different GameData object will be created for each thread which is parsing telemetry
 *
 * TODO: I want GameData to be a dumb pojo
 * TODO: I want a Spring managed threadsafe bean does all the calculating of stuff given a GameData object, lookups by game id?
 * TODO: ...at the end of the day, I really want to be able to wire in "calculators" for different data points in a clean way
 */
public class GameData {

    private String gameId;
    private String mapName;
    private Instant startTime;
    private List<Player> winners;   //this isn't actually winners, it is the last people alive

    /**
     * This will hold information about what is on the map. Should know where red/blue, white zone are.
     * Should know where packages are and what time they landed.
     */
    private List<GameState> gameStates;
    private Map<Integer, Set<String>> teamData;
    private Map<String, List<PlayerState>> playerStateMap; //TODO: this should really just be player positions or something and should be nested in a PlayerData object
    private Map<String, Double> playerKillsMap;

    //if there are more than this many enemies in a zone, we'll just record 15, which will make the data easier to normalize
    static int ENEMY_NUMBER_CAP = 15;
    static double NEAREST_TEAM_MEMBER_CAP = 50000.0;
    static double NEAREST_ENEMY_CAP = 50000.0;
    static double DISTANCE_TO_SAFE_ZONE_CAP = 250000.0;
    static double EIGHT_KM_MAP_SIZE = 816000.0;
    static double HEIGHT_CAP = 100000.0;
    static double KILL_CAP = 12;
    static double MAX_PLAYERS = 100;


    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
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

    public List<Player> getWinners() {
        if(winners == null){
            return new ArrayList<>();
        }
        return winners;
    }

    public void setWinners(List<Player> winners) {
        this.winners = winners;
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

    public Map<String, Double> getPlayerKillsMap() {
        if(playerKillsMap == null){
            this.playerKillsMap = new HashMap<>();
        }
        return playerKillsMap;
    }

    public void setPlayerKillsMap(Map<String, Double> playerKillsMap) {
        this.playerKillsMap = playerKillsMap;
    }

    public Map<String, SortedMap<String, String>> getPhasedPlayerDataPoints(){
        Stream<Map.Entry<String, SortedMap<String, String>>> result =
                Stream.of(
                        getPlayerDataPoints("1.0").entrySet().stream(),
                        getPlayerDataPoints("2.0").entrySet().stream(),
                        getPlayerDataPoints("3.0").entrySet().stream(),
                        getPlayerDataPoints("4.0").entrySet().stream(),
                        getPlayerDataPoints("5.0").entrySet().stream(),
                        getPlayerDataPoints("6.0").entrySet().stream(),
                        getPlayerDataPoints("7.0").entrySet().stream(),
                        getPlayerDataPoints("8.0").entrySet().stream()
                ).flatMap(Function.identity());
        return result.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private Map<String, SortedMap<String, String>> getPlayerDataPoints(String gamePhase){
        Map<String, SortedMap<String, String>> returnMap = new HashMap<>();

        //it is not guaranteed a particular game will progress through all phases, if it doesn't no further processing
        if(getGameStateByPhase(gamePhase) == null){
            return returnMap;
        }

        Map<String, PlayerState> currentPhasePlayerStates = getStatesByPhase(gamePhase);
        Map<String, PlayerState> nextPhasePlayerStates = getStatesByPhase(String.valueOf(Double.parseDouble(gamePhase) + 1.0));

        //for each player in this list, I will build an entry in the map
        for(String playerName : currentPhasePlayerStates.keySet()){

            PlayerState currentPlayerState = currentPhasePlayerStates.get(playerName);

            SortedMap<String, String> statsMap = new TreeMap<>();
            /*
                Check to see if the player is alive
                - if there is a PlayerState entry for the player in the next phase they are alive
                - if the player lives until the end of the game, they are alive
             */
            statsMap.put("alive", (nextPhasePlayerStates.get(playerName) != null ||
                    winners.stream().map(Player::getName).collect(Collectors.toSet()).contains(playerName)) ? "1" : "0");

            statsMap.put("mapErangel", ("Erangel_Main".equals(getMapName()) || "Baltic_Main".equals(getMapName())) ? "1.0" : "0.0");
            statsMap.put("mapMiramar", "Desert_Main".equals(getMapName()) ? "1.0" : "0.0");

            //will return number between 0 and NEAREST_ENEMY_CAP
            statsMap.put("closestEnemyDistance", calculateClosestEnemyDistance(currentPlayerState.getPlayer(), gamePhase, new RangeMapper(0.0, NEAREST_ENEMY_CAP)));


            //number between 0 and DISTANCE_TO_SAFE_ZONE_CAP
            statsMap.put("distanceToSafeZone", calculateDistanceToSafeZone(currentPlayerState.getPlayer(), gamePhase, new RangeMapper(0.0, DISTANCE_TO_SAFE_ZONE_CAP)));

            RangeMapper enemyRangeMapper = new RangeMapper(0.0, ENEMY_NUMBER_CAP);

            //will return number between 0 and ENEMY_NUMBER_CAP
            statsMap.put("enemyCountZeroToTwentyFive", calculateEnemiesWithinDistance(currentPlayerState.getPlayer(), gamePhase, 0.0, 25.0, enemyRangeMapper));
            statsMap.put("enemyCountTwentyFiveToFifty", calculateEnemiesWithinDistance(currentPlayerState.getPlayer(), gamePhase, 25.0, 50.0, enemyRangeMapper));
            statsMap.put("enemyCountFiftyToOneHundred", calculateEnemiesWithinDistance(currentPlayerState.getPlayer(), gamePhase, 50.0, 100.0, enemyRangeMapper));
            statsMap.put("enemyCountOneHundredToTwoFifty", calculateEnemiesWithinDistance(currentPlayerState.getPlayer(), gamePhase, 100.0, 250.0, enemyRangeMapper));

            //which game phase we are in, we'll do up to 7.0 for now
            statsMap.put("gamePhase1", "1.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase2", "2.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase3", "3.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase4", "4.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase5", "5.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase6", "6.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase7", "7.0".equals(gamePhase) ? "1.0" : "0.0");
            statsMap.put("gamePhase8", "8.0".equals(gamePhase) ? "1.0" : "0.0");

            //return number between 0 and NEAREST_TEAM_MEMBER_CAP
            statsMap.put("nearestTeamMember", calculateNearestTeamMember(currentPlayerState.getPlayer(), gamePhase, new RangeMapper(0.0, NEAREST_TEAM_MEMBER_CAP)));

            //return between 0 and 3
            statsMap.put("numAliveTeamMembers", calculateAliveTeammates(currentPlayerState.getPlayer().getTeamId(), currentPlayerState.getPlayer().getName(), gamePhase, new RangeMapper(0.0, 3.0)));

            statsMap.put("xPosition", calculatePositionValue(currentPlayerState.getPlayer().getLocation().getX(), new RangeMapper(0.0, EIGHT_KM_MAP_SIZE)));
            statsMap.put("yPosition", calculatePositionValue(currentPlayerState.getPlayer().getLocation().getY(), new RangeMapper(0.0, EIGHT_KM_MAP_SIZE)));
            statsMap.put("zPosition", calculatePositionValue(currentPlayerState.getPlayer().getLocation().getZ(), new RangeMapper(0.0, HEIGHT_CAP)));

            statsMap.put("killsCount", calculateTotalKills(playerKillsMap.get(currentPlayerState.getPlayer().getName()) == null ? 0.0 : playerKillsMap.get(currentPlayerState.getPlayer().getName()), new RangeMapper(0.0, KILL_CAP)));
            statsMap.put("numberOfAlivePlayers", calculateNumberOfAlivePlayers(currentPlayerState, new RangeMapper(0.0, MAX_PLAYERS)));

            returnMap.put(currentPlayerState.getPlayer().getName() + "_" + gamePhase, statsMap);
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

    private String calculateTotalKills(Double kills, RangeMapper rangeMapper){
        return String.format("%.2f", rangeMapper.apply(kills));
    }

    private String calculateNumberOfAlivePlayers(PlayerState playerState, RangeMapper rangeMapper){
        return String.format("%.2f", rangeMapper.apply((double) playerState.getNumAlivePlayers()));
    }

    //small optimization could be made here to not recalculate this multiple times
    private GameState getGameStateByPhase(String gamePhase){
        for(GameState gameState : this.gameStates){
            if(gamePhase.equals(gameState.getGamePhase())){
                return gameState;
            }
        }
        return null;
    }

    //this is called multiple times for each of the enemy within range calculations
    //could optimize here to find enemies by team number instead and have some sort of cache for that
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
}
