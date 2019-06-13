package com.github.ryanp102694.pubgtelemetryparser.data.event;

import java.time.Instant;

/**
 *  * gamePhase = 0 -> Before lift off
 *  * gamePhase = 0.1 -> On airplane
 *  * gamePhase = 0.5 -> When there’s no ‘zone’ on map(before game starts)
 *  * gamePhase = 1.0 -> First safezone and bluezone appear
 *  * gamePhase = 1.5 -> First bluezone shrinks
 *  * gamePhase = 2.0 -> Second bluezone appears
 *  * gamePhase = 2.5 -> Second bluezone shrinks
 */

public class Common {
    private Instant time;
    private String gamePhase;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getGamePhase() {
        return gamePhase;
    }

    public void setGamePhase(String gamePhase) {
        this.gamePhase = gamePhase;
    }
}
