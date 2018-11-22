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

public class UserTank {

    private Bitmap mBitmap;
    private DatabaseReference mUserDataRef;
    private Position firePosition;
    private float mX, mY, mDegrees;
    private int mMapHeight, mMapWidth;
    private long prevTime;

    private static final String TAG = "WL UserTank";
    private static final String USERS_KEY = "users";
    private static final String USER_ID_KEY = "userId";
    private static final String POS_KEY = Globals.POS_KEY;
    private static final String FIRE_KEY = Globals.FIRE_KEY;
    private static final String X_KEY = Globals.X_KEY;
    private static final String Y_KEY = Globals.Y_KEY;
    private static final String DEGREES_KEY = Globals.DEGREES_KEY;
    private static final double SPEED_SCALE_FACTOR = 0.4;

    /**
     * Constructor function for the User Tank class.
     *
     * @param activity      the activity in which the player tank is instantiated
     */
    UserTank(Activity activity) {

        // Get the blue tank bitmap since it is the user's tank
        mBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.blue_tank);

        // Get the user document from Firestore
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String userId = sharedPref.getString(USER_ID_KEY, "");
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        if (userId.length() > 0) {
            mUserDataRef = database.child(USERS_KEY).child(userId);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }


        // Set the initial x and y coordinate for the tank
        mX = 100;
        mY = 100;


        // Get the width and height of the map
        mMapWidth = Resources.getSystem().getDisplayMetrics().widthPixels - 120;
        mMapHeight = Resources.getSystem().getDisplayMetrics().heightPixels - 620;
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

        // Set the new x and y coordinates assuming the tank can move there
        mX += velocityX * deltaTime * SPEED_SCALE_FACTOR;
        mY += velocityY * deltaTime * SPEED_SCALE_FACTOR;

        // Force the tank to remain within the map horizontally
        if (mX < 0) {
            mX = 0;
        } else if (mX > mMapWidth) {
            mX = mMapWidth;
        }

        // Force the tank to remain within the map vertically
        if (mY < 0) {
            mY = 0;
        } else if (mY > mMapHeight) {
            mY = mMapHeight;
        }

        // Only change the angle if the tank has moved
        if (velocityX != 0 || velocityY != 0) {
            mDegrees = angle;
            updateDataRef(POS_KEY, new Position(mX, mY,mDegrees));
        }
    }

    public void setFirePosition() {
        updateDataRef(FIRE_KEY, new Position(mX, mY, mDegrees));
    }


    /**
     * Accesses the user's data in the Firebase database with a key and
     * updates the data with a new value.
     *
     * @param key   the key of the data to be updated
     * @param value the value of the new data
     */
    private void updateDataRef(final String key, final Object value) {
        mUserDataRef.child(key)
                .setValue(value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
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
