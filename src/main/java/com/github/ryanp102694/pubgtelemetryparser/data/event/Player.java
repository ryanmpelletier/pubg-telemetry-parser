package com.github.ryanp102694.pubgtelemetryparser.data.event;

import org.json.JSONObject;

/**
 * {
 *   "name":         string,
 *   "teamId":       int,
 *   "health":       number,
 *   "location":     Location,
 *   "ranking":      int,
 *   "accountId":    string
 *   "isInBlueZone": bool,
 *   "isInRedZone":  bool,
 *   "zone":         [regionId, ...]
 * }
 */
public class Player implements FromJsonObject<Player> {

    private String name;
    private int teamId;
    private double health;
    private Location location;
    private int ranking;
    private String accountId;
    private boolean isInBlueZone;
    private boolean isInRedZone;


    @Override
    public Player fromJSONObject(JSONObject jsonObject) {

        Player player = new Player();
        player.setName(jsonObject.getString("name"));
        player.setTeamId(jsonObject.getInt("teamId"));
        player.setHealth(jsonObject.getDouble("health"));
        player.setLocation(new Location().fromJSONObject(jsonObject.getJSONObject("location")));
        player.setRanking(jsonObject.getInt("ranking"));
        player.setAccountId(jsonObject.getString("accountId"));
        player.setInBlueZone(jsonObject.getBoolean("isInBlueZone"));
        player.setInRedZone(jsonObject.getBoolean("isInRedZone"));

        return player;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isInBlueZone() {
        return isInBlueZone;
    }

    public void setInBlueZone(boolean inBlueZone) {
        isInBlueZone = inBlueZone;
    }

    public boolean isInRedZone() {
        return isInRedZone;
    }

    public void setInRedZone(boolean inRedZone) {
        isInRedZone = inRedZone;
    }
}
