package com.wztlei.tanktrouble.battle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wztlei.tanktrouble.Globals;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;

public class UserTank {

    private Bitmap mBitmap;
    private DatabaseReference mUserDataRef;
    private float mX, mY, mDegrees;
    private int mMinX, mMaxX;
    private int mMinY, mMaxY;
    private long prevTime;

    private static final String TAG = "WL/UserTank";
    private static final String USERS_KEY = "users";
    private static final String POS_KEY = Globals.POS_KEY;
    private static final String FIRE_KEY = Globals.FIRE_KEY;
    private static final float SPEED_SCALE = 0.4f;
    private static final float TANK_WIDTH_SCALE = Globals.TANK_WIDTH_SCALE;
    private static final float TANK_HEIGHT_SCALE = Globals.TANK_HEIGHT_SCALE;

    /**
     * Constructor function for the User Tank class.
     *
     * @param activity      the activity in which the player tank is instantiated
     */
    UserTank(Activity activity) {
        int tankWidth = Math.round(UserUtils.getScreenWidth() * TANK_WIDTH_SCALE);
        int tankHeight = Math.round(UserUtils.getScreenWidth() * TANK_HEIGHT_SCALE);

        // Get the blue tank bitmap since it is the user's tank
        mBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.blue_tank);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, tankWidth, tankHeight, false);

        // Get the user document from Firestore
        String userId = UserUtils.getUserId();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        if (userId.length() > 0) {
            mUserDataRef = database.child(USERS_KEY).child(userId);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }


        // Set the initial x and y coordinate for the tank
        mX = 100;
        mY = 300;

        // Get the boundaries of the map
        int screenWidth = UserUtils.getScreenWidth();

        mMinX = 0;
        mMaxX = screenWidth;
        mMinY = Math.round(screenWidth * Globals.TANK_MIN_Y_SCALE);
        mMaxY = Math.round(screenWidth * Globals.TANK_MAX_Y_SCALE);
    }

    /*private BroadcastReceiver createBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isConnected = intent.getBooleanExtra
                        (ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                if(isConnected){
                    Log.d(TAG, "Connected to network");
                }else{
                    Log.d(TAG, "Connection Error");

                    // Build an alert dialog using the title and message
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Connection Error")
                            .setMessage("Check your internet connection and try again.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            });

                    // Get the AlertDialog from create() and show it
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        };
    }*/

    /**
     * Draws the tank bitmap onto a canvas with the proper rotation.
     *
     * @param canvas the canvas on which the tank is drawn.
     */
    public void draw(Canvas canvas) {

        Matrix matrix = new Matrix();
        matrix.setRotate(mDegrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
        canvas.drawBitmap(rotatedBitmap, mX, mY, null);
    }

    /**
     * Sets the new x and y coordinates and the new angle for the tank while ensuring that
     * the tank remains within the map boundaries.
     *
     * @param velocityX the velocity that the tank intends to move in the x direction
     * @param velocityY the velocity that the tank intends to move in the y direction
     * @param angle     the new angle of the tank
     */
    public void moveAndRotate(float velocityX, float velocityY, int angle) {
        long newTime = System.currentTimeMillis();
        long deltaTime = newTime - prevTime;
        prevTime = newTime;

        float oldX = mX;
        float oldY = mY;

        // Set the new x and y coordinates assuming the tank can move there
        mX += velocityX * deltaTime * SPEED_SCALE;
        mY += velocityY * deltaTime * SPEED_SCALE;

        // Force the tank to remain within the map horizontally
        if (mX < mMinX || mX > mMaxX) {
            mX = oldX;
        }

        // Force the tank to remain within the map vertically
        if (mY < mMinY || mY > mMaxY) {
            mY = oldY;
        }

        /* TODO: Stop move tank if hitting a wall?
        if (mX < 0 || mX > mMapWidth || mY < 0 || mY > mMapHeight) {
            mX = oldX;
            mY = oldY;
        }*/

        // Only change the angle if the tank has moved
        if (velocityX != 0 || velocityY != 0) {
            mDegrees = angle;
            Position position = new Position(mX, mY, mDegrees, false);
            position.standardizePosition();
            updateDataRef(POS_KEY, position);
        }
    }

    public void setFirePosition() {
        Position position = new Position(mX, mY, mDegrees, false);
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

    public float getAngle() {
        return mDegrees;
    }
}
