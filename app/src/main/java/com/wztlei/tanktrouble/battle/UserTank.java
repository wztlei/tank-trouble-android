package com.wztlei.tanktrouble.battle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wztlei.tanktrouble.Globals;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.map.MapUtils;

public class UserTank {

    private Bitmap mBitmap;
    private DatabaseReference mUserDataRef;
    private int mX, mY, mDeg;
    private long lastTime;
    private int mWidth, mHeight;

    private static final String TAG = "WL/UserTank";
    private static final String USERS_KEY = "users";
    private static final String POS_KEY = Globals.POS_KEY;
    private static final String FIRE_KEY = Globals.FIRE_KEY;
    private static final float SPEED_CONST = 0.4f;
    private static final float TANK_WIDTH_CONST = Globals.TANK_WIDTH_CONST;
    private static final float TANK_HEIGHT_CONST = Globals.TANK_HEIGHT_CONST;

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

        // Get the user document from Firestore
        String userId = UserUtils.getUserId();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        if (userId != null && userId.length() > 0) {
            mUserDataRef = database.child(USERS_KEY).child(userId);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }

        // TODO: Set random initial coordinates
        // Set the initial x and y coordinate for the tank
        mX = 100;
        mY = 300;
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

//        PointF[] tankPolygon = MapUtils.tankPolygon(mX, mY, mDeg, mWidth, mHeight);
//        Paint paint = new Paint();
//        paint.setARGB(255, 200, 50, 50);
//
//        for (PointF pointF : tankPolygon) {
//            canvas.drawCircle(pointF.x, pointF.y, 5, paint);
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
    public void moveAndRotate(float velocityX, float velocityY, float angle) {
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
     * Sets the position value in Firebase to the location where the user last fired.
     */
    public void setFirePosition() {
        Position position = new Position(mX, mY, mDeg, false);
        position.standardizePosition();
        updateDataRef(FIRE_KEY, position);
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
