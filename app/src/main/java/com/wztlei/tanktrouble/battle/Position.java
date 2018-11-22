package com.wztlei.tanktrouble.battle;

public class Position {

    Position(int x, int y, int deg) {
        this.x = x;
        this.y = y;
        this.deg = deg;
    }

    Position(float x, float y, float deg) {
        this.x = (int) x;
        this.y = (int) y;
        this.deg = (int) deg;
    }

    public int x;
    public int y;
    public int deg;
}
