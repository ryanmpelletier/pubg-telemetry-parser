package com.github.ryanp102694.pubgtelemetryparser.data;

import com.github.ryanp102694.pubgtelemetryparser.data.event.Common;
import com.github.ryanp102694.pubgtelemetryparser.data.event.Player;

import java.time.Instant;

/**
 * This object will hold various data points about a player in a PUBG game at different times.
 * Ultimately, this playerState will represent one trainingPoint about players.
 */
public class PlayerState extends Common {

    private Player player;
    private int numAlivePlayers;
    private int elapsedTime;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getNumAlivePlayers() {
        return numAlivePlayers;
    }

    public void setNumAlivePlayers(int numAlivePlayers) {
        this.numAlivePlayers = numAlivePlayers;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
