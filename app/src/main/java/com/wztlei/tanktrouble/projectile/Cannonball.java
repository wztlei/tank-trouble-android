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
    private int mX, mY, mDeg;
    private boolean mEscapingCollision;

    private static final String TAG = "WL/Cannonball";
    private static final float SPEED_CONST = UserUtils.scaleGraphics(32/1080f)/100f;
    private static final float RADIUS = UserUtils.scaleGraphics(Constants.CANNONBALL_RADIUS_CONST);
    private static final int TEST_DIST = Math.round(RADIUS + 2);



    public Cannonball(int x, int y, int deg) {
        mX = x;
        mY = y;

        if (MapUtils.validCannonballPosition(x, y, RADIUS)) {
            mDeg = deg;
            mEscapingCollision = false;
        } else {
            handleWallCollision(x, y, deg);
            mEscapingCollision = true;
        }

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
        int newX = mX + deltaX;
        int newY = mY + deltaY;

        //Log.d(TAG, "deltaX" + deltaX + " deltaY" + deltaY);

        // No need to proceed since the cannonball will disappear after colliding with the user
        if (userTank.detectCollision(newX, newY, RADIUS)) {
            mX = newX;
            mY = newY;
            // TODO: Undo test
            //return true;
        }

        if (MapUtils.validCannonballPosition(newX, newY, RADIUS)) {
            mX = newX;
            mY = newY;
            mEscapingCollision = false;
        }
//        else if (mEscapingCollision) {
//            mX = newX;
//            mY = newY;
//        }
        else {
            mX = newX;
            mY = newY;
            handleWallCollision(newX, newY, mDeg);
        }

        return false;
    }

    private void handleWallCollision(int x, int y, int deg) {
        boolean horizontalCollision = false;
        boolean verticalCollision = false;
        mDeg = deg;
        mEscapingCollision = true;
        Log.d(TAG, "handleWallCollision");

        int newX = x, newY = y;

        if (MapUtils.validCannonballPosition(x-TEST_DIST, y, RADIUS)) {
            Log.d(TAG, "pure vertical 1");
            verticalCollision = true;

            while (!MapUtils.validCannonballPosition(newX, y, RADIUS)) {
                newX--;
            }
        }

        if (MapUtils.validCannonballPosition(x+TEST_DIST, y, RADIUS) && !verticalCollision) {
            Log.d(TAG, "pure vertical 2");
            verticalCollision = true;

            while (!MapUtils.validCannonballPosition(newX, y, RADIUS)) {
                newX++;
            }
        }

        if (MapUtils.validCannonballPosition(x, y-TEST_DIST, RADIUS)) {
            Log.d(TAG, "pure horizontal 1");
            horizontalCollision = true;

            while (!MapUtils.validCannonballPosition(x, newY, RADIUS)) {
                newY--;
            }
        }

        if (MapUtils.validCannonballPosition(x, y+TEST_DIST, RADIUS) && !horizontalCollision) {
            Log.d(TAG, "pure horizontal 2");
            horizontalCollision = true;

            while (!MapUtils.validCannonballPosition(x, newY, RADIUS)) {
                newY++;
            }
        }

        if (!horizontalCollision && !verticalCollision) {
            if (isBetween(-180, deg, -90)
                    && MapUtils.validCannonballPosition(x+TEST_DIST, y+TEST_DIST, RADIUS)) {

                while (!MapUtils.validCannonballPosition(newX, newY, RADIUS)) {
                    newX++;
                    newY++;
                }

                horizontalCollision = true;
                verticalCollision = true;
                Log.d(TAG, "[-180, -90]");
            } else if (isBetween(-90, deg, 0)
                    && MapUtils.validCannonballPosition(x-TEST_DIST, y+TEST_DIST, RADIUS)) {

                while (!MapUtils.validCannonballPosition(newX, newY, RADIUS)) {
                    newX--;
                    newY++;
                }

                horizontalCollision = true;
                verticalCollision = true;
                Log.d(TAG, "[-90, 0]");
            } else if (isBetween(0, deg, 90)
                    && MapUtils.validCannonballPosition(x-TEST_DIST, y-TEST_DIST, RADIUS)) {

                while (!MapUtils.validCannonballPosition(newX, newY, RADIUS)) {
                    newX--;
                    newY--;
                }

                horizontalCollision = true;
                verticalCollision = true;
                Log.d(TAG, "[0, 90]");
            } else if (isBetween(90, deg, 180)
                    && MapUtils.validCannonballPosition(x+TEST_DIST, y-TEST_DIST, RADIUS)) {

                while (!MapUtils.validCannonballPosition(newX, newY, RADIUS)) {
                    newX++;
                    newY--;
                }

                horizontalCollision = true;
                verticalCollision = true;
                Log.d(TAG, "[90, 180]");
            }
        }

        mX = newX;
        mY = newY;

        if (verticalCollision) {
            Log.d(TAG, "vertical");
            mDeg = horizontallyReflectDeg(mDeg);
        }

        if (horizontalCollision) {
            Log.d(TAG, "horizontal");
            mDeg = verticallyReflectDeg(mDeg);
        }

        if (!horizontalCollision && !verticalCollision) {
            Log.e(TAG, "no collision = contradiction");
        }
    }

    private int horizontallyReflectDeg (int deg) {
        if (deg == 0) {
            return 180;
        } else {
            return (int) Math.signum(deg)*180 - deg;
        }
    }

    private int verticallyReflectDeg (int deg) {
        return -deg;
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

    /**
     * Returns true if num is in the interval [min, max] and false otherwise.
     *
     * @param min   the minimum value of num
     * @param num   the number to test
     * @param max   the maximum value of num
     * @return      true if num is in the interval [min, max] and false otherwise
     */
    private static boolean isBetween (float min, float num, float max) {
        return (min <= num && num <= max);
    }

    public long getFiringTime() {
        return mFiringTime;
    }
}
