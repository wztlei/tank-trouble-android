package com.wztlei.tanktrouble.cannonball;

import com.wztlei.tanktrouble.UserUtils;

public class Coordinate {
    public int x, y;

    private static final float SCREEN_SCALE = UserUtils.getScreenScale();

    // Firebase's Realtime Database requires a no-argument constructor
    @SuppressWarnings("unused")
    public Coordinate() {}

    Coordinate(float x, float y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public Coordinate standardized() {
        return new Coordinate(x / SCREEN_SCALE, y / SCREEN_SCALE);
    }

    public void scale() {
        x *= SCREEN_SCALE;
        y *= SCREEN_SCALE;
    }
}
