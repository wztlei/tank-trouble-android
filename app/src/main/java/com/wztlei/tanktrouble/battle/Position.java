package com.wztlei.tanktrouble.battle;

import com.wztlei.tanktrouble.UserUtils;

public class Position {

    public float x, y, deg;
    private int rand;

    private static final float SCREEN_SCALE = UserUtils.getScreenScale();

    // Firebase's Realtime Database requires a no-argument constructor
    @SuppressWarnings("unused")
    Position(){}

    public Position(float x, float y, float deg) {
        this.x = x;
        this.y = y;
        this.deg = deg;
    }

    public void standardizePosition() {
        x /= SCREEN_SCALE;
        y /= SCREEN_SCALE;
    }

    public void scalePosition() {
        x *= SCREEN_SCALE;
        y *= SCREEN_SCALE;
    }
}
