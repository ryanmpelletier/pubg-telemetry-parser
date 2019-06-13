package com.github.ryanp102694.pubgtelemetryparser.data.event;

import org.json.JSONObject;

public class Location implements FromJsonObject<Location>{

    private double x;
    private double y;
    private double z;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double distanceBetween(Location otherLocation){
        return Math.sqrt(Math.pow((this.x - otherLocation.getX()), 2.0) + Math.pow((this.y - otherLocation.getY()), 2.0) + Math.pow((this.z - otherLocation.getZ()), 2.0));
    }

    @Override
    public Location fromJSONObject(JSONObject jsonObject) {
        Location location = new Location();
        location.setX(jsonObject.getDouble("x"));
        location.setY(jsonObject.getDouble("y"));
        location.setZ(jsonObject.getDouble("z"));
        return location;
    }
}
