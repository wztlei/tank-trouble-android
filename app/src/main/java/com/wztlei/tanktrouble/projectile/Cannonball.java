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
    private int mX, mY, mDeg;
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
        mX = x;
        mY = y;

        if (MapUtils.cannonballWallCollision(x, y, RADIUS)) {
            handleWallCollision(x, y, deg);
        } else {
            mDeg = deg;
        }

        mPath = generatePath(x, y, deg);
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
        mX += deltaX;
        mY += deltaY;


        // No need to proceed since the cannonball will disappear after colliding with the user
        if (userTank != null && userTank.detectCollision(mX, mY, RADIUS)) {
            // TODO: Uncomment to detect user tank collision
            //return true;
        }

        // Detect a wall collision and handle it appropriately
        if (MapUtils.cannonballWallCollision(mX, mY, RADIUS)) {
            handleWallCollision(mX, mY, mDeg);
        }

        return false;
    }

    /**
     * Handles a collision with a wall for a cannonball at (x, y) travelling in a direction of deg
     * degrees. The method updates the location and direction of the cannonball accordingly.
     *
     * @param x     the x-coordinate of the cannonball
     * @param y     the y-coordinate of the cannonball
     * @param deg   the angle of the direction of the cannonball's movement in degrees
     */
    private void handleWallCollision(int x, int y, int deg) {
        int newX = x, newY = y;
        boolean horizontalCollision = false;
        boolean verticalCollision = false;
        mDeg = deg;

        // Handle a cannonball colliding vertically with a wall to the right
        if (!MapUtils.cannonballWallCollision(x-TEST_DIST, y, RADIUS)) {
            verticalCollision = true;

            // Move the cannonball until we reach a valid position
            while (MapUtils.cannonballWallCollision(newX, y, RADIUS)) {
                newX--;
            }
        }

        // Handle a cannonball colliding vertically with a wall to the left
        if (!MapUtils.cannonballWallCollision(x+TEST_DIST, y, RADIUS) && !verticalCollision) {
            verticalCollision = true;

            // Move the cannonball until we reach a valid position
            while (MapUtils.cannonballWallCollision(newX, y, RADIUS)) {
                newX++;
            }
        }

        // Handle a cannonball colliding horizontally with a wall to the top
        if (!MapUtils.cannonballWallCollision(x, y-TEST_DIST, RADIUS) && !verticalCollision) {
            horizontalCollision = true;

            // Move the cannonball until we reach a valid position
            while (MapUtils.cannonballWallCollision(x, newY, RADIUS)) {
                newY--;
            }
        }

        // Handle a cannonball colliding horizontally with a wall to the bottom
        if (!MapUtils.cannonballWallCollision(x, y+TEST_DIST, RADIUS)
                && !verticalCollision && !horizontalCollision) {
            horizontalCollision = true;

            // Move the cannonball until we reach a valid position
            while (MapUtils.cannonballWallCollision(x, newY, RADIUS)) {
                newY++;
            }
        }

        // We have not detected a horizontal or vertical collision yet,
        // so we try detecting a collision with an inward facing corner
        if (!horizontalCollision && !verticalCollision) {
            // Determine the angle of collision since each angle can only
            // collide with one of the four corner orientations
            if (inRange(-180, deg, -90)
                    && !MapUtils.cannonballWallCollision(x+TEST_DIST, y+TEST_DIST, RADIUS)) {
                horizontalCollision = true;
                verticalCollision = true;

                // Move the cannonball until we reach a valid position
                while (MapUtils.cannonballWallCollision(newX, newY, RADIUS)) {
                    newX++;
                    newY++;
                }
            } else if (inRange(-90, deg, 0)
                    && !MapUtils.cannonballWallCollision(x-TEST_DIST, y+TEST_DIST, RADIUS)) {
                horizontalCollision = true;
                verticalCollision = true;

                // Move the cannonball until we reach a valid position
                while (MapUtils.cannonballWallCollision(newX, newY, RADIUS)) {
                    newX--;
                    newY++;
                }
            } else if (inRange(0, deg, 90)
                    && !MapUtils.cannonballWallCollision(x-TEST_DIST, y-TEST_DIST, RADIUS)) {
                horizontalCollision = true;
                verticalCollision = true;

                // Move the cannonball until we reach a valid position
                while (MapUtils.cannonballWallCollision(newX, newY, RADIUS)) {
                    newX--;
                    newY--;
                }
            } else if (inRange(90, deg, 180)
                    && !MapUtils.cannonballWallCollision(x+TEST_DIST, y-TEST_DIST, RADIUS)) {
                horizontalCollision = true;
                verticalCollision = true;

                // Move the cannonball until we reach a valid position
                while (MapUtils.cannonballWallCollision(newX, newY, RADIUS)) {
                    newX++;
                    newY--;
                }
            }
        }

        // Update the x and y coordinates
        mX = newX;
        mY = newY;

        // Horizontally reflect the angle for a vertical collision
        if (verticalCollision) {
            mDeg = horizontallyReflectDeg(mDeg);
        }

        // Vertically reflect the angle for a horizontal collision
        if (horizontalCollision) {
            mDeg = verticallyReflectDeg(mDeg);
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

    public int getY() {
        return mY;
    }

    public long getFiringTime() {
        return mFiringTime;
    }
}
