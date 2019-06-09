package com.github.ryanp102694.pubgtelemetryparser.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Will store all game info as game progresses.
 * Should be able to query this object with a time and get the state of all players of the game, dead or alive.
 *
 * A different GameData object will be created for each thread which is parsing telemetry
 */
public class GameData {

    private String gameId;

    /**
     * This will hold information about what is on the map. Should know where red/blue, white zone are.
     * Should know where packages are and what time they landed.
     */
    private List<MapState> mapStates;
    private Map<String, Set<String>> teamData;
    private Map<String, List<PlayerState>> playerStateMap;


    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public List<MapState> getMapStates() {
        return mapStates;
    }

    public void setMapStates(List<MapState> mapStates) {
        this.mapStates = mapStates;
    }

    public Map<String, Set<String>> getTeamData() {
        return teamData;
    }

    public void setTeamData(Map<String, Set<String>> teamData) {
        this.teamData = teamData;
    }

    public Map<String, List<PlayerState>> getPlayerStateMap() {
        return playerStateMap;
    }

    public void setPlayerStateMap(Map<String, List<PlayerState>> playerStateMap) {
        this.playerStateMap = playerStateMap;
    }
}
