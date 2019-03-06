package com.wztlei.tanktrouble.battle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.map.MapUtils;

import java.util.Random;

public class UserTank {

    private Bitmap mBitmap;
    private DatabaseReference mUserDataRef;
    private int mX, mY, mDeg;
    private long lastTime;
    private int mWidth, mHeight;

    private static final String TAG = "WL/UserTank";
    private static final String USERS_KEY = Constants.USERS_KEY;
    private static final String POS_KEY = Constants.POS_KEY;
    private static final String FIRE_KEY = Constants.FIRE_KEY;
    private static final float SPEED_CONST = UserUtils.scaleGraphics(40/1080f)/100f;
    private static final float TANK_WIDTH_CONST = Constants.TANK_WIDTH_CONST;
    private static final float TANK_HEIGHT_CONST = Constants.TANK_HEIGHT_CONST;

    /**
     * Constructor function for the User Tank class.
     *
     * @param activity      the activity in which the player tank is instantiated
     */
    UserTank(Activity activity) {
        mWidth = Math.max(Math.round(UserUtils.scaleGraphics(TANK_WIDTH_CONST)), 1);
        mHeight = Math.max(Math.round(UserUtils.scaleGraphics(TANK_HEIGHT_CONST)), 1);

        // Get the blue tank bitmap since it is the user's tank
        mBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.blue_tank);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mWidth, mHeight, false);

        // Get the user document from Firebase
        String userId = UserUtils.getUserId();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        if (userId != null && userId.length() > 0) {
            mUserDataRef = database.child(USERS_KEY).child(userId);
            mUserDataRef.child(FIRE_KEY).setValue(null);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }

        // Set the initial x and y coordinates for the tank
        do {
            mX = randomInt(0, UserUtils.getScreenWidth());
            mY = randomInt((int) UserUtils.scaleGraphics(Constants.MAP_TOP_Y_CONST),
                    (int) UserUtils.scaleGraphics(Constants.MAP_TOP_Y_CONST+1));
            mDeg = randomInt(-180, 180);
        } while (!MapUtils.validTankPosition(mX, mY, mDeg, mWidth, mHeight));
    }

    /**
     * Draws the tank bitmap onto a canvas with the proper rotation.
     *
     * @param canvas the canvas on which the tank is drawn.
     */
    public void draw(Canvas canvas) {
        Matrix matrix = new Matrix();
        matrix.setRotate(mDeg);
        Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
        canvas.drawBitmap(rotatedBitmap, mX, mY, null);

//        PointF[] tankPolygon = MapUtils.tankHitbox(mX, mY, mDeg, mWidth, mHeight);
//        Paint paint = new Paint();
//        paint.setARGB(255, 200, 50, 50);
//
//        for (PointF pointF : tankPolygon) {
//            canvas.drawCircle(pointF.x, pointF.y, 10, paint);
//        }
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

                // Check whether the new position is valid
                if (!MapUtils.validTankPosition(mX + deltaX, mY, angle, mWidth, mHeight)) {
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

                // Check whether the new position is valid
                if (!MapUtils.validTankPosition(mX, mY + deltaY, angle, mWidth, mHeight)) {
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
                MapUtils.validTankPosition(mX, mY, angle, mWidth, mHeight))) {
            mX += deltaX;
            mY += deltaY;
            mDeg = (int) angle;
        } else {
            // Get the original polygon and get the two old centers of rotation
            PointF[] oldPolygon = MapUtils.tankPolygon(mX, mY, mDeg, mWidth, mHeight);
            PointF oldFrontCenter = oldPolygon[0];
            PointF oldMidCenter = oldPolygon[1];

            // Get the new polygon with rotation, but not movement,
            // and get the two new centers of rotation
            PointF[] newPolygon = MapUtils.tankPolygon(mX, mY, angle, mWidth, mHeight);
            PointF newFrontCenter = newPolygon[0];
            PointF newMidCenter = newPolygon[1];

            // Get test values of x and y where the rotation occurs,
            // but the rotational centers remain unmoved
            int testX1 = Math.round(mX + oldMidCenter.x - newMidCenter.x);
            int testY1 = Math.round(mY + oldMidCenter.y - newMidCenter.y);
            int testX2 = Math.round(mX + oldFrontCenter.x - newFrontCenter.x);
            int testY2 = Math.round(mY + oldFrontCenter.y - newFrontCenter.y);

            // Try rotating while keeping either rotational center unmoved
            if (MapUtils.validTankPosition(testX1, testY1, angle, mWidth, mHeight)) {
                mX = testX1;
                mY = testY1;
                mDeg = (int) angle;
            } else if (MapUtils.validTankPosition(testX2, testY2, angle, mWidth, mHeight)) {
                mX = testX2;
                mY = testY2;
                mDeg = (int) angle;
            }
        }

        Position position = new Position(mX, mY, mDeg, false);
        position.standardizePosition();
        updateDataRef(POS_KEY, position);
        lastTime = nowTime;
    }

    /**
     * Returns true if a cannonball hit the tank and false otherwise.
     *
     * @param   cannonballX         the x coordinate of the cannonball
     * @param   cannonballY         the y coordinate of the cannonball
     * @param   cannonballRadius    the radius of the cannonball
     * @return                      true if a cannonball hit the tank and false otherwise
     */
    public boolean detectCollision(float cannonballX, float cannonballY, float cannonballRadius) {
        PointF[] hitbox = MapUtils.tankHitbox(mX, mY, mDeg, mWidth, mHeight);

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
     * Sets the position value in Firebase to the location where the user last fired.
     */
    public void setFirePosition(Position position) {
        if (!position.isStandardized) {
            position.standardizePosition();

        }

        position.rand = randomInt(0, 999999999);
        updateDataRef(FIRE_KEY, position);
    }

    /**
     * Gets the position of the front of the gun when the tank fired.
     */
    public Position getFirePosition (){
        PointF[] tankPolygon = MapUtils.tankPolygon(mX, mY, mDeg, mWidth, mHeight);
        float fireX = tankPolygon[0].x;
        float fireY = tankPolygon[0].y;

        return new Position(fireX, fireY, mDeg, false);
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

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public float getDegrees() {
        return mDeg;
    }
}
