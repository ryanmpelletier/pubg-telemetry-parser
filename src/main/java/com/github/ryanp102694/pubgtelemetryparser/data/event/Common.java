package com.github.ryanp102694.pubgtelemetryparser.data.event;

import java.time.Instant;

/**
 *  * isGame = 0 -> Before lift off
 *  * isGame = 0.1 -> On airplane
 *  * isGame = 0.5 -> When there’s no ‘zone’ on map(before game starts)
 *  * isGame = 1.0 -> First safezone and bluezone appear
 *  * isGame = 1.5 -> First bluezone shrinks
 *  * isGame = 2.0 -> Second bluezone appears
 *  * isGame = 2.5 -> Second bluezone shrinks
 */

public class Common {
    private Instant time;
    private String isGame;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getIsGame() {
        return isGame;
    }

    public void setIsGame(String isGame) {
        this.isGame = isGame;
    }
}
