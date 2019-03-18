package com.wztlei.tanktrouble.battle;

import com.wztlei.tanktrouble.UserUtils;

public class Coordinate {
    public int x, y;

    private static final float SCREEN_SCALE = UserUtils.getScreenScale();

    public Coordinate(float x, float y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public void standardizeCoordinate() {
        x /= SCREEN_SCALE;
        y /= SCREEN_SCALE;
    }

    public void scaleCoordinate() {
        x *= SCREEN_SCALE;
        y *= SCREEN_SCALE;
    }
}
