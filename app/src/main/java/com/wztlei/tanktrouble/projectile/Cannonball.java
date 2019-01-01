package com.wztlei.tanktrouble.projectile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.UserTank;
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
     * Update the location and direction of the cannonball.
     *
     * @return true if a cannonball collided with the user, and false otherwise
     */
    public boolean updateAndDetectUserCollision(UserTank userTank) {
        // Store relevant time information
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - mLastTime;
        mLastTime = nowTime;

        // Get the displacement in the x and y directions
        float distance = deltaTime * SPEED_CONST;
        int deltaX = (int) Math.round(distance * Math.cos(Math.toRadians(mDeg)));
        int deltaY = (int) Math.round(distance * Math.sin(Math.toRadians(mDeg)));
        float newX = mX + deltaX;
        float newY = mY + deltaY;
        int unitX = (int) Math.signum(deltaX);
        int unitY = (int) Math.signum(deltaY);

        // No need to proceed since the cannonball will disappear after colliding with the user
        if (userTank.detectCollision(newX, newY, RADIUS)) {
            mX = newX;
            mY = newY;
            return true;
        }

        if (MapUtils.validCannonballPosition(newX, newY, RADIUS)) {
            mX = newX;
            mY = newY;
            return false;
        }

        // Determine how the cannonball should reflect if it hits a wall
        while (true) {
            // Try decreasing the x displacement
            if (deltaX != 0) {
                deltaX -= unitX;
                newX = mX + deltaX;
            }

            // If changing the x displacement made the cannonball position valid,
            // then the cannonball collided with a vertical edge.
            if (MapUtils.validCannonballPosition(newX, newY, RADIUS)) {
                mX = newX;
                mY = newY;

                if (mDeg == 0) {
                    mDeg = 180;
                } else {
                    mDeg = Math.signum(mDeg)*180 - mDeg;
                }
                break;
            }

            // Try decreasing the y displacement
            if (deltaY != 0) {
                deltaY -= unitY;
                newY = mY + deltaY;
            }

            // If changing the y displacement made the cannonball position valid,
            // then the cannonball collided with a horizontal edge.
            if (MapUtils.validCannonballPosition(newX, newY, RADIUS)) {
                mX = newX;
                mY = newY;
                mDeg = -mDeg;
                break;
            }

            // Emergency break of the loop
            if (deltaX == 0 && deltaY == 0) {
                break;
            }
        }

        // Move in the x and y directions
        mX += deltaX;
        mY += deltaY;

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
