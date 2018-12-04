package com.wztlei.tanktrouble.battle;

import java.util.HashMap;

public class MapTile {

    private HashMap<Wall, Boolean> mWalls;

    public static final int SIDE_LENGTH = 100;

    public enum Wall {
        TOP, RIGHT, BOTTOM, LEFT
    }

    public MapTile(boolean top, boolean right, boolean bottom, boolean left) {
        mWalls.put(Wall.TOP, top);
        mWalls.put(Wall.RIGHT, right);
        mWalls.put(Wall.BOTTOM, bottom);
        mWalls.put(Wall.LEFT, left);
    }

    public boolean withinWalls (int x, int y) {
        return true;
    }

}
