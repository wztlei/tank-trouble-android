package com.wztlei.tanktrouble.battle;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wztlei.tanktrouble.R;

import java.util.Random;

public class PlayerTank {

    private Bitmap mBitmap;
    private DocumentReference mUserDocument;
    private float mOldX, mOldY, mOldAngle;
    private float mX, mY, mAngle;
    private int mMapHeight, mMapWidth;

    private static final String TAG = "PlayerTank.java";
    private static final String USERS_KEY = "users";
    private static final String USER_ID_KEY = "userId";
    private static final String X_FIELD = "x";
    private static final String Y_FIELD = "y";
    private static final String ANGLE_FIELD = "angle";
    private static final double SPEED = 0.04;


    /**
     * Constructor function for the Player Tank class.
     *
     * @param activity      the activity in which the player tank is instantiated
     * @param isUserTank    stores whether it is a user's or an opponent's tank
     */
    PlayerTank(Activity activity, boolean isUserTank) {

        BroadcastReceiver broadcastReceiver = createBroadcastReceiver();

        // Get the blue tank bitmap if it is the user tank or
        // get the red tank bitmap if it is an opponent's tank
        if (isUserTank) {
            mBitmap = BitmapFactory.decodeResource
                    (activity.getResources(), R.drawable.blue_tank);

            // Get the user document from Firestore
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
            String mUserId = sharedPref.getString(USER_ID_KEY, "");
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            mUserDocument = firestore.collection(USERS_KEY).document(mUserId);
        } else {
            mBitmap = BitmapFactory.decodeResource
                    (activity.getResources(), R.drawable.red_tank);
        }

        // Set the initial x and y coordinate for the tank
        if (isUserTank) {
            mX = 100;
            mY = 100;
        } else {
            mX = 300;
            mY = 100;
        }

        // Get the width and height of the map
        mMapWidth = Resources.getSystem().getDisplayMetrics().widthPixels - 120;
        mMapHeight = Resources.getSystem().getDisplayMetrics().heightPixels - 600;
    }

    private BroadcastReceiver createBroadcastReceiver() {
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
    }

    /**
     * Draws the tank bitmap onto a canvas with the proper rotation.
     *
     * @param canvas the canvas on which the tank is drawn.
     */
    public void draw(Canvas canvas) {

        Matrix matrix = new Matrix();
        matrix.setRotate(mAngle);
        Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
        canvas.drawBitmap(rotatedBitmap, mX, mY, null);

        /*matrix.setRotate(mOldAngle);
        Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
        canvas.drawBitmap(rotatedBitmap, mOldX, mOldY, null);*/
    }

    /**
     * Sets the new x and y coordinates and the new angle for the tank while ensuring that
     * the tank remains within the map boundaries.
     *
     * @param deltaX    the displacement that the tank intends to move in the x direction
     * @param deltaY    the displacement that the tank intends to move in the y direction
     * @param angle     the new angle of the tank
     */
    public void moveAndRotate(int deltaX, int deltaY, int angle) {

        float oldX = mX;
        float oldY = mY;
        float oldAngle = mAngle;

        // Set the new x and y coordinates assuming the tank can move there
        mX += SPEED * deltaX;
        mY += SPEED * deltaY;

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
        if (deltaX != 0 || deltaY != 0) {
            mAngle = angle;
        }

        updateUserDocumentFloat(X_FIELD, mX);
        updateUserDocumentFloat(Y_FIELD, mY);
        updateUserDocumentFloat(ANGLE_FIELD, mAngle);
        //mX = oldX;
        //Random random = new Random();
        //int rand = random.nextInt(1000);

//        mY = oldY;
//        mAngle = oldAngle;
    }

    private void updateUserDocumentFloat(final String field, final float value) {


        mUserDocument.update(field, value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        switch (field) {
                            case X_FIELD:
                                mOldX = mX;
                                Log.d(TAG, "updateUserDocumentFloat x");
                                break;
                            case Y_FIELD:
                                mOldY = mY;
                                Log.d(TAG, "updateUserDocumentFloat y");
                                break;
                            case ANGLE_FIELD:
                                mOldAngle = mAngle;
                                Log.d(TAG, "updateUserDocumentFloat angle");
                                break;
                        }
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
        return mAngle;
    }
}
