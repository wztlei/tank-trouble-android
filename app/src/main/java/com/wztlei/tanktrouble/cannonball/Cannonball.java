package com.wztlei.tanktrouble.cannonball;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.map.MapUtils;

import java.util.ArrayList;

public class Cannonball {

    private ArrayList<Coordinate> mPath;
    private int mPrevPathIndex;
    private float mX, mY;
    private long mFiringTime, mLastTime;
    private int mUUID;

    //private static final String TAG = "WL/Cannonball";
    private static final float SPEED =
            UserUtils.scaleGraphicsFloat(Constants.CANNONBALL_SPEED_CONST);
    private static final int RADIUS =
            UserUtils.scaleGraphicsInt(Constants.CANNONBALL_RADIUS_CONST);
    private static final int CANNONBALL_LIFESPAN = Constants.CANNONBALL_LIFESPAN;
    private static final int START_FADING_AGE = 9800;
    private static final int BUFFER = 1000;
    private static final float CANNONBALL_DISTANCE = SPEED * CANNONBALL_LIFESPAN + BUFFER;

    /**
     * Constructor method for a cannonball created by the user tank.
     *
     * @param x     the x-coordinate from which the cannonball was fired
     * @param y     the y-coordinate from which the cannonball was fired
     * @param deg   the angle in degrees at which the cannonball was fired
     */
    public Cannonball(int x, int y, int deg, int uuid) {
        mPath = generatePath(x, y, deg);
        mPrevPathIndex = 0;
        mX = x;
        mY = y;
        mFiringTime = System.currentTimeMillis();
        mLastTime = mFiringTime;
        mUUID = uuid;
    }

    /**
     * Constructor method for a cannonball created by an opponent tank.
     *
     * @param path  the path of the cannonball
     */
    Cannonball(ArrayList<Coordinate> path) {
        mPath = path;
        mPrevPathIndex = 0;
        mX = mPath.get(0).x;
        mY = mPath.get(0).y;
        mFiringTime = System.currentTimeMillis();
        mLastTime = mFiringTime;
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
                float xyDist = calcDistance(dx, dy);
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
                travelDistance += calcDistance(dx, dy);
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
                travelDistance += calcDistance(dx, dy);
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
        float movementDist = deltaTime * SPEED;

        // Continue moving the cannonball while it still have distance left to move
        while (movementDist > 0) {
            // Get the points of the path on either side of the cannonball's location
            Coordinate prevPathCoord = mPath.get(mPrevPathIndex);
            Coordinate nextPathCoord = mPath.get(mPrevPathIndex + 1);

            // Get the distance between the adjacent points in the path
            float pathDx = nextPathCoord.x - prevPathCoord.x;
            float pathDy = nextPathCoord.y - prevPathCoord.y;
            float pathDist = calcDistance(pathDx, pathDy);

            // Calculate the distance the cannonball will travel
            float dx = movementDist * pathDx / pathDist;
            float dy = movementDist * pathDy / pathDist;
            float prevDist = calcDistance(prevPathCoord.x, prevPathCoord.y, mX+dx, mY+dy);

            // Determine if we will go past the next coordinate in the path
            if (prevDist > pathDist) {
                // Move onto the next coordinate in the path while decrement the distance travelled
                mPrevPathIndex++;
                mX = nextPathCoord.x;
                mY = nextPathCoord.y;
                movementDist -= calcDistance(mX, mY, nextPathCoord.x, nextPathCoord.y);
            } else {
                // We have finished moving so we set movementDist to zero
                mX += dx;
                mY += dy;
                movementDist = 0;
            }
        }
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

    public Path getStandardizedPath() {
        ArrayList<Coordinate> standardizedCoords = new ArrayList<>();

        for (Coordinate coordinate : mPath) {
            standardizedCoords.add(coordinate.standardized());
        }

        return new Path(standardizedCoords, mUUID);
    }

    /**
     * Returns the distance between points (x1, y1) and (x2, y2).
     *
     * @param x1    the x-coordinate of point 1
     * @param y1    the y-coordinate of point 1
     * @param x2    the x-coordinate of point 2
     * @param y2    the y-coordinate of point 2
     * @return      the distance between point 1 and point 2
     */
    private float calcDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }

    /**
     * Returns the distance from moving dx in the x direction and y in the y direction.
     *
     * @param dx    displacement in the x direction
     * @param dy    displacement in the y direction
     * @return      the distance moved
     */
    private float calcDistance(float dx, float dy) {
        return (float) Math.sqrt(dx*dx + dy*dy);
    }

    public int getX() {
        return (int) mX;
    }

    public int getY() {
        return (int) mY;
    }

    public int getRadius() {
        return RADIUS;
    }

    public long getFiringTime() {
        return mFiringTime;
    }

    public int getUUID() {
        return mUUID;
    }


}
