package com.wztlei.tanktrouble.projectile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.Coordinate;
import com.wztlei.tanktrouble.battle.UserTank;
import com.wztlei.tanktrouble.map.MapUtils;

import java.util.ArrayList;

public class Cannonball {

    private ArrayList<Coordinate> mPath;
    private int mPathIndex;
    private int mX, mY;
    private long mFiringTime, mLastTime;


    private static final String TAG = "WL/Cannonball";
    private static final float SPEED_CONST =
            UserUtils.scaleGraphicsFloat(Constants.CANNONBALL_SPEED_CONST);
    private static final int RADIUS = UserUtils.scaleGraphicsInt(Constants.CANNONBALL_RADIUS_CONST);
    private static final int TEST_DIST = Math.round(RADIUS + 2);
    private static final int CANNONBALL_LIFESPAN = Constants.CANNONBALL_LIFESPAN;
    private static final int START_FADING_AGE = 9800;
    private static final float CANNONBALL_DISTANCE = SPEED_CONST * CANNONBALL_LIFESPAN;

    public Cannonball(int x, int y, int deg) {
        mPath = generatePath(x, y, deg);
        mPathIndex = 0;
        mX = x;
        mY = y;
        mFiringTime = System.currentTimeMillis();
        mLastTime = mFiringTime;
    }

    public Cannonball(ArrayList<Coordinate> path) {
        mPath = path;
    }

    /**
     * Returns the path of a cannonball ball fired from (startX, startY) at an angle of mDeg, which
     * contains the starting coordinate, collision points with walls, and the ending coordinate.
     *
     * @param startX    starting x-coordinate of the cannonball
     * @param startY    starting y-coordinate of the cannonball
     * @param deg       starting angle of the cannonball
     * @return          the path of the cannonball
     */
    private ArrayList<Coordinate> generatePath(int startX, int startY, int deg) {
        ArrayList<Coordinate> path = new ArrayList<>();
        float x = startX;
        float y = startY;
        float dx = RADIUS * (float) Math.cos(Math.toRadians(deg));
        float dy = RADIUS * (float) Math.sin(Math.toRadians(deg));
        float travelDistance = 0;

        // Add the starting coordinate
        if (!MapUtils.cannonballWallCollision(x, y, RADIUS)) {
            path.add(new Coordinate(x, y));
        }

        // Continue adding coordinates to the path while the max distance hasn't been reached yet
        while (travelDistance < CANNONBALL_DISTANCE) {
            // Check for a collision with a wall
            if (MapUtils.cannonballWallCollision(x, y, RADIUS)) {
                // Determine whether the collision was horizontal, vertical, or both
                boolean collisionRetreatX = MapUtils.cannonballWallCollision(x-dx, y, RADIUS);
                boolean collisionRetreatY = MapUtils.cannonballWallCollision(x, y-dy, RADIUS);
                boolean horizontalWallCollision = collisionRetreatX || !collisionRetreatY;
                boolean verticalWallCollision = collisionRetreatY || !collisionRetreatX;

                // Initialize variables to determine a valid position for a cannonbal
                float xyDist = (float) Math.sqrt(dx*dx + dy*dy);
                float testX = x, testY = y, testDist = 1;

                // Loop until we find a coordinate (testX, testY) that does not collide with a wall
                while (MapUtils.cannonballWallCollision(testX, testY, RADIUS)) {
                    testX = x - (testDist * dx/xyDist);
                    testY = y - (testDist * dy/xyDist);
                    testDist++;
                }

                // Update x, y, travelDistance, and path
                x = testX;
                y = testY;
                travelDistance += Math.sqrt(dx*dx + dy*dy);
                path.add(new Coordinate(x, y));

                // Reflect dy for a collision with a horizontal wall
                if (horizontalWallCollision) {
                    dy = -dy;
                }

                // Reflect dx for a collision with a vertical wall
                if (verticalWallCollision) {
                    dx = -dx;
                }
            } else {
                x += dx;
                y += dy;
                travelDistance += Math.sqrt(dx*dx + dy*dy);
            }
        }

        path.add(new Coordinate(x, y));
        return path;
    }

    /**
     * Updates the location and direction of the cannonball.
     */
    public void update() {
        // Store relevant time information
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - mLastTime;
        mLastTime = nowTime;

        // Get the displacement in the x and y directions
        float distance = deltaTime * SPEED_CONST;

        // TODO: Move the cannonball using the path variable

    }

    /**
     * Draws the cannonball onto a canvas. Fades out the cannonball in the
     * last 200 ms of its lifespan.
     *
     * @param canvas the canvas on which the cannonball is drawn.
     */
    public void draw(Canvas canvas) {
        long nowTime = System.currentTimeMillis();
        long ageTime = nowTime - mFiringTime;

//        if (mPath != null) {
//            for (int i = 0; i < mPath.size(); i++) {
//                Paint paint = new Paint();
//                paint.setARGB(255, 255, 0, 0);
//                canvas.drawCircle(mPath.get(i).x, mPath.get(i).y, RADIUS, paint);
//            }
//        }

        // Determine whether to fade the cannonball or not
        if (ageTime <= 9800) {
            // Draw a black cannonball in the first 9800 ms of its lifespan
            Paint paint = new Paint();
            paint.setARGB(255, 0, 0, 0);
            canvas.drawCircle(mX, mY, RADIUS, paint);
        } else {
            // Fade out the cannonball in the last 200 ms of its lifespan
            int tint = (int) Math.max(255 * (CANNONBALL_LIFESPAN - ageTime)/
                    (CANNONBALL_LIFESPAN-START_FADING_AGE), 0);
            Paint paint = new Paint();
            paint.setARGB(tint, 0, 0, 0);
            canvas.drawCircle(mX, mY, RADIUS, paint);
        }
    }

    /**
     * Returns true if num is in the interval [min, max] and false otherwise.
     *
     * @param min   the minimum value of num
     * @param num   the number to test
     * @param max   the maximum value of num
     * @return      true if num is in the interval [min, max] and false otherwise
     */
    private static boolean inRange (float min, float num, float max) {
        return (min <= num && num <= max);
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public int getRadius() {
        return RADIUS;
    }

    public long getFiringTime() {
        return mFiringTime;
    }
}
