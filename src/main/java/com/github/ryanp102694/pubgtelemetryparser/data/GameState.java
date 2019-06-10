package com.github.ryanp102694.pubgtelemetryparser.data;


import com.github.ryanp102694.pubgtelemetryparser.data.event.Common;
import com.github.ryanp102694.pubgtelemetryparser.data.event.Location;


public class GameState extends Common {

    private int elapsedTime;
    private int numAliveTeams;
    private int numJoinPlayers;
    private int numStartPlayers;
    private int numAlivePlayers;
    private double safetyZoneRadius;
    private Location safetyZonePosition;
    private double poisonGasWarningRadius;
    private Location poisonGasWarningPosition;
    private double redZoneRadius;
    private Location redZonePosition;

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getNumAliveTeams() {
        return numAliveTeams;
    }

    public void setNumAliveTeams(int numAliveTeams) {
        this.numAliveTeams = numAliveTeams;
    }

    public int getNumJoinPlayers() {
        return numJoinPlayers;
    }

    public void setNumJoinPlayers(int numJoinPlayers) {
        this.numJoinPlayers = numJoinPlayers;
    }

    public int getNumStartPlayers() {
        return numStartPlayers;
    }

    public void setNumStartPlayers(int numStartPlayers) {
        this.numStartPlayers = numStartPlayers;
    }

    public int getNumAlivePlayers() {
        return numAlivePlayers;
    }

    public void setNumAlivePlayers(int numAlivePlayers) {
        this.numAlivePlayers = numAlivePlayers;
    }

    public Location getSafetyZonePosition() {
        return safetyZonePosition;
    }

    public void setSafetyZonePosition(Location safetyZonePosition) {
        this.safetyZonePosition = safetyZonePosition;
    }

    public Location getPoisonGasWarningPosition() {
        return poisonGasWarningPosition;
    }

    public void setPoisonGasWarningPosition(Location poisonGasWarningPosition) {
        this.poisonGasWarningPosition = poisonGasWarningPosition;
    }

    public Location getRedZonePosition() {
        return redZonePosition;
    }

    public void setRedZonePosition(Location redZonePosition) {
        this.redZonePosition = redZonePosition;
    }

    public double getSafetyZoneRadius() {
        return safetyZoneRadius;
    }

    public void setSafetyZoneRadius(double safetyZoneRadius) {
        this.safetyZoneRadius = safetyZoneRadius;
    }

    public double getPoisonGasWarningRadius() {
        return poisonGasWarningRadius;
    }

    public void setPoisonGasWarningRadius(double poisonGasWarningRadius) {
        this.poisonGasWarningRadius = poisonGasWarningRadius;
    }

    public double getRedZoneRadius() {
        return redZoneRadius;
    }

    public void setRedZoneRadius(double redZoneRadius) {
        this.redZoneRadius = redZoneRadius;
    }
}
