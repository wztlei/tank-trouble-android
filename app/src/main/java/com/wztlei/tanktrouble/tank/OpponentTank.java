package com.wztlei.tanktrouble.tank;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.Position;

public class OpponentTank extends Tank {
    private DatabaseReference mPosDataRef;

    private static final String TAG = "WL/OpponentTank";


    /**
     * @param activity      the activity in which the opponent tank is initialized
     * @param opponentId    the Firebase ID of the opponent
     */
    public OpponentTank(Activity activity, String opponentId, TankColor tankColor) {
        mWidth = Math.max(UserUtils.scaleGraphicsInt(TANK_WIDTH_CONST), 1);
        mHeight = Math.max(UserUtils.scaleGraphicsInt(TANK_HEIGHT_CONST), 1);

        // Get the tank bitmap
        mColorIndex = tankColor.getIndex();
        mBitmap = tankColor.getTankBitmap(activity);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mWidth, mHeight, false);

        // Get the user document from Firebase
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        if (opponentId.length() > 0) {
            mPosDataRef = database.child(USERS_KEY).child(opponentId).child(POS_KEY);
            addPosDataRefListeners();

            Log.d(TAG, "opponentId=" + opponentId);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }
    }

    /**
     * Add listeners for the position of the opponent tank.
     */
    private void addPosDataRefListeners() {
        // Get initial position
        mPosDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get position object and use the values to update the UI
                Position position = dataSnapshot.getValue(Position.class);

                if (position != null) {
                    position.scalePosition();
                    mX = (int) position.x;
                    mY = (int) position.y;
                    mDeg = (int) position.deg;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // Listen for position changes
        mPosDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get position object and use the values to update the UI
                Position position = dataSnapshot.getValue(Position.class);

                if (position != null) {
                    position.scalePosition();
                    mX = (int) position.x;
                    mY = (int) position.y;
                    mDeg = (int) position.deg;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
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
    }
}
