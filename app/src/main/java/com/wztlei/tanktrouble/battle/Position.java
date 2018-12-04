package com.wztlei.tanktrouble.battle;

import android.util.Log;

import com.google.firebase.database.Exclude;
import com.wztlei.tanktrouble.UserUtils;

public class Position {


    /*Position(int x, int y, int deg) {
        this.x = x;
        this.y = y;
        this.deg = deg;
    }*/

    /*Position(float x, float y, float deg) {
        this.x = (int) x;
        this.y = (int) y;
        this.deg = (int) deg;
    }

    public int x;
    public int y;
    public int deg;*/

    Position() {}

    public float x, y, deg;

    @Exclude
    private boolean isStandardized;

    Position(float x, float y, float deg, boolean isStandardized) {
        this.x = x;
        this.y = y;
        this.deg = deg;
        this.isStandardized = isStandardized;
    }

    public void setIsStandardized(boolean isStandardized) {
        this.isStandardized = isStandardized;
    }

    public void standardizePosition() {
        if (!isStandardized) {
            float screenScale = UserUtils.getScreenScale();
            x /= screenScale;
            y /= screenScale;
            isStandardized = true;
        }
    }

    public void scalePosition() {
        if (isStandardized) {
            float screenScale = UserUtils.getScreenScale();
            x *= screenScale;
            y *= screenScale;
            isStandardized = false;
        }
    }


}
