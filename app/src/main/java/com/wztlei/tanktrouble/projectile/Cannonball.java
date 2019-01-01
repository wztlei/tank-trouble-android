package com.wztlei.tanktrouble.projectile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.map.MapUtils;

public class Cannonball {

    private long mFiringTime, mLastTime;
    private float mX, mY, mDeg;

    private static final String TAG = "WL/Cannonball";
    private static final float SPEED_CONST = UserUtils.scaleGraphics(32/1080f)/100f;
    private static final float RADIUS = UserUtils.scaleGraphics(Constants.CANNONBALL_RADIUS_CONST);


    public Cannonball(float x, float y, float deg) {
        mX = x;
        mY = y;
        mDeg = deg;
        mFiringTime = System.currentTimeMillis();
        mLastTime = mFiringTime;
    }

    /**
     * Updates all the location and direction of the cannonball.
     *
     * @return true if a cannonball collided with the user, and false otherwise
     */
    public boolean updateAndDetectUserCollision() {
        // Store relevant time information
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - mLastTime;
        mLastTime = nowTime;

        // Get the displacement in the x and y directions
        float distance = deltaTime * SPEED_CONST;
        float maxDeltaX = Math.round(distance * Math.cos(Math.toRadians(mDeg)));
        float maxDeltaY = Math.round(distance * Math.sin(Math.toRadians(mDeg)));

        // Move in the x and y directions
        mX += maxDeltaX;
        mY += maxDeltaY;

        return false;
    }

    /**
     * Draws the cannonball onto a canvas.
     *
     * @param canvas the canvas on which the cannonball is drawn.
     */
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setARGB(255, 0, 0, 0);
        canvas.drawCircle(mX, mY, RADIUS, paint);
    }

    public long getFiringTime() {
        return mFiringTime;
    }
}
