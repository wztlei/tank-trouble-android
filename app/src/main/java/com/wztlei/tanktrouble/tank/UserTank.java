package com.wztlei.tanktrouble.tank;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.Position;
import com.wztlei.tanktrouble.cannonball.Cannonball;
import com.wztlei.tanktrouble.map.MapUtils;

import java.util.Random;

public class UserTank extends Tank {

    private DatabaseReference mUserDataRef;
    private long lastTime;

    private static final String TAG = "WL/UserTank";
    private static final float SPEED_CONST = UserUtils.scaleGraphicsFloat(40/1080f)/100f;

    /**
     * Constructor function for the User Tank class.
     *
     * @param activity      the activity in which the player tank is instantiated
     */
    public UserTank(Activity activity, TankColor tankColor) {
        mWidth = Math.max(UserUtils.scaleGraphicsInt(TANK_WIDTH_CONST), 1);
        mHeight = Math.max(UserUtils.scaleGraphicsInt(TANK_HEIGHT_CONST), 1);

        // Get the tank bitmap and color
        mBitmap = tankColor.getTankBitmap(activity);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mWidth, mHeight, false);
        mColorIndex = tankColor.getIndex();
        mScore = 0;
        mIsAlive = true;

        // Get the user document from Firebase
        String userId = UserUtils.getUserId();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        if (userId != null && userId.length() > 0) {
            mUserDataRef = database.child(USERS_KEY).child(userId);
            mUserDataRef.child(FIRE_KEY).removeValue();
        } else {
            Log.e(TAG, "Warning: no user Id");
        }

        // Set the initial x and y coordinates for the tank
        do {
            mX = randomInt(50, UserUtils.getScreenWidth());
            mY = randomInt(UserUtils.scaleGraphicsInt(1.1f * Constants.MAP_TOP_Y_CONST),
                    UserUtils.scaleGraphicsInt(0.9f*Constants.MAP_TOP_Y_CONST + 1));
            mDeg = randomInt(-180, 180);
        } while (MapUtils.tankWallCollision(mX, mY, mDeg, mWidth, mHeight));
    }

    /**
     * Sets the new x and y coordinates and the new angle for the tank while ensuring that
     * the tank remains within the map boundaries.
     *
     * @param velocityX the velocity that the tank intends to move in the x direction
     * @param velocityY the velocity that the tank intends to move in the y direction
     * @param angle     the new angle of the tank
     */
    public void update(float velocityX, float velocityY, float angle) {
        // Get the difference in time between now and the last movement
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - lastTime;

        // Store the maximum displacement allowed in the x and y directions
        int maxDeltaX = Math.round(velocityX*deltaTime*SPEED_CONST);
        int maxDeltaY = Math.round(velocityY*deltaTime*SPEED_CONST);

        // Initialize variables for the while loop
        int deltaX = 0, deltaY = 0;
        int unitX = (int) Math.signum(maxDeltaX), unitY = (int) Math.signum(maxDeltaY);
        boolean reachedMaxX = false, reachedMaxY = false;

        // Loop until we have reached the max x and max y displacement
        while (!reachedMaxX || !reachedMaxY) {
            // Check whether we can still move in the x direction
            if (!reachedMaxX) {

                // Try increasing the movement in the x direction
                deltaX += unitX;

                // Check whether the new position collides with a wall
                if (MapUtils.tankWallCollision(mX + deltaX, mY, angle, mWidth, mHeight)) {
                    // Record that an invalid position has been reached and reset deltaX
                    reachedMaxX = true;
                    deltaX -= unitX;
                }

                // Check whether we have reached the maximum allowed value of deltaX
                if (deltaX == maxDeltaX) {
                    reachedMaxX = true;
                }
            }

            // Check whether we can still move in the y direction
            if (!reachedMaxY) {

                // Try increasing the movement in the y direction
                deltaY += unitY;

                // Check whether the new position collides with a wall
                if (MapUtils.tankWallCollision(mX, mY + deltaY, angle, mWidth, mHeight)) {
                    // Record that an invalid position has been reached and reset deltaY
                    reachedMaxY = true;
                    deltaY -= unitY;
                }

                // Check whether we have reached the maximum allowed value of deltaY
                if (deltaY == maxDeltaY) {
                    reachedMaxY = true;
                }
            }
        }

        // Check whether movement or rotation is possible with either deltaX, deltaY, or angle
        // and move accordingly
        if (deltaX != 0 || deltaY != 0 || (velocityX == 0 && velocityY == 0 &&
                !MapUtils.tankWallCollision(mX, mY, angle, mWidth, mHeight))) {
            mX += deltaX;
            mY += deltaY;
            mDeg = (int) angle;
        } else {
            // Get the original polygon and get the two old centers of rotation
            PointF[] oldPolygon = Tank.tankPolygon(mX, mY, mDeg, mWidth, mHeight);
            PointF oldFrontCenter = oldPolygon[0];
            PointF oldMidCenter = oldPolygon[1];

            // Get the new polygon with rotation, but not movement,
            // and get the two new centers of rotation
            PointF[] newPolygon = Tank.tankPolygon(mX, mY, angle, mWidth, mHeight);
            PointF newFrontCenter = newPolygon[0];
            PointF newMidCenter = newPolygon[1];

            // Get test values of x and y where the rotation occurs,
            // but the rotational centers remain unmoved
            int testX1 = Math.round(mX + oldMidCenter.x - newMidCenter.x);
            int testY1 = Math.round(mY + oldMidCenter.y - newMidCenter.y);
            int testX2 = Math.round(mX + oldFrontCenter.x - newFrontCenter.x);
            int testY2 = Math.round(mY + oldFrontCenter.y - newFrontCenter.y);

            // Try rotating while keeping either rotational center unmoved
            if (!MapUtils.tankWallCollision(testX1, testY1, angle, mWidth, mHeight)) {
                mX = testX1;
                mY = testY1;
                mDeg = (int) angle;
            } else if (!MapUtils.tankWallCollision(testX2, testY2, angle, mWidth, mHeight)) {
                mX = testX2;
                mY = testY2;
                mDeg = (int) angle;
            }
        }

        Position position = new Position(mX, mY, mDeg);
        position.standardizePosition();
        updateDataRef(POS_KEY, position);
        lastTime = nowTime;
    }

    /**
     * Returns true if a cannonball hit the tank and false otherwise.
     *
     * @param   cannonball         the x coordinate of the cannonball
     * @return                      true if a cannonball hit the tank and false otherwise
     */
    public boolean detectCollision(Cannonball cannonball) {
        PointF[] hitbox = Tank.tankHitbox(mX, mY, mDeg, mWidth, mHeight);
        int cannonballX = cannonball.getX();
        int cannonballY = cannonball.getY();
        int cannonballRadius = cannonball.getRadius();

        // Iterate through all the points in the tank's hitbox
        for (PointF pointF : hitbox) {
            if (calcDistance(pointF.x, pointF.y, cannonballX, cannonballY) < cannonballRadius) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates the Euclidean distance between two points, given a displacement in the x-axis
     * and a displacement in the y-axis using the Pythagorean theorem.
     *
     * @param x1    the x-coordinate of the first point
     * @param y1    the y-coordinate of the first point
     * @param x2    the x-coordinate of the second point
     * @param y2    the y-coordinate of the second point
     * @return      the distance between the two points
     */
    private static float calcDistance (float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }

    /**
     * Returns the position of the front of the gun when the tank fired and
     * sets the position value in Firebase to the location where the user last fired.
     */
    public Cannonball fire() {
        PointF[] tankPolygon = Tank.tankPolygon(mX, mY, mDeg, mWidth, mHeight);

        Cannonball c = new Cannonball((int) tankPolygon[0].x, (int) tankPolygon[0].y, mDeg,
                UserUtils.randomInt(1, Integer.MAX_VALUE-10));
        updateDataRef(FIRE_KEY, c.getStandardizedPath());

        return c;
    }

    public void kill(int killingCannonball) {
        updateDataRef(Constants.DEATH_KEY, killingCannonball);
        incrementScore();
    }

    public void respawn(){
        updateDataRef(Constants.DEATH_KEY, null);
    }

    public void reset() {
        mUserDataRef.removeValue();
    }

    /**
     * Accesses the user's data in the Firebase database with a key and
     * updates the data with a new value.
     *
     * @param key   the key of the data to be updated
     * @param value the value of the new data
     */
    private void updateDataRef(final String key, final Object value) {
        mUserDataRef.child(key).setValue(value);
    }

    /**
     * Generates a random number on the closed interval [min, max].
     *
     * @param min   the minimum number that can be generated
     * @param max   the maximum number that can be generated
     * @return      the random number between min and max
     */
    private int randomInt (int min, int max){
        Random random = new Random();
        return random.nextInt(max-min+1) + min;
    }

    public int getDegrees() {
        return mDeg;
    }
}
