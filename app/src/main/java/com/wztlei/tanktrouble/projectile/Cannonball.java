package com.wztlei.tanktrouble.projectile;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;

public class Cannonball {

    private int mX, mY, mDeg;
    private int radius = (int) UserUtils.scaleGraphics(Constants.CANNONBALL_RADIUS_CONST);

    public Cannonball(int x, int y, int deg) {
        mX = x;
        mY = y;
        mDeg = deg;
    }

    /**
     * Draws the cannonball onto a canvas.
     *
     * @param canvas the canvas on which the cannonball is drawn.
     */
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setARGB(255, 60, 60, 60);
        canvas.drawCircle(mX, mY, radius, paint);
    }
}
