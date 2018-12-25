package com.wztlei.tanktrouble.map;

public class MapCell {

    private boolean mTopWall, mRightWall, mBottomWall, mLeftWall;

    MapCell(String code) {
        mLeftWall = code.contains("L");
        mTopWall = code.contains("T");
        mRightWall = code.contains("R");
        mBottomWall = code.contains("B");
    }

    public boolean hasLeftWall() {
        return mLeftWall;
    }

    public boolean hasTopWall() {
        return mTopWall;
    }

    public boolean hasRightWall() {
        return mRightWall;
    }

    public boolean hasBottomWall() {
        return mBottomWall;
    }
}
