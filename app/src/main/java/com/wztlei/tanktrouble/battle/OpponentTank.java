package com.wztlei.tanktrouble.battle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;

public class OpponentTank {
    private Bitmap mBitmap;
    private DatabaseReference mPosDataRef;
    private float mX, mY, mDeg;

    private static final String TAG = "WL/UserTank";
    private static final String USERS_KEY = Constants.USERS_KEY;
    private static final String POS_KEY = Constants.POS_KEY;
    private static final float TANK_WIDTH_CONST = Constants.TANK_WIDTH_CONST;
    private static final float TANK_HEIGHT_CONST = Constants.TANK_HEIGHT_CONST;

    OpponentTank(Activity activity, String opponentId) {
        int tankWidth = Math.round(UserUtils.scaleGraphics(TANK_WIDTH_CONST));
        int tankHeight = Math.round(UserUtils.scaleGraphics(TANK_HEIGHT_CONST));

        // Get the red tank bitmap since it is an opponent's tank
        mBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.red_tank);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, tankWidth, tankHeight, false);

        // Get the user document from Firestore
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        if (opponentId.length() > 0) {
            mPosDataRef = database.child(USERS_KEY).child(opponentId).child(POS_KEY);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }

        // Get initial position
        mPosDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get position object and use the values to update the UI
                Position position = dataSnapshot.getValue(Position.class);

                if (position != null) {
                    position.setIsStandardized(true);
                    position.scalePosition();
                    mX = position.x;
                    mY = position.y;
                    mDeg = position.deg;
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
                    position.setIsStandardized(true);
                    position.scalePosition();
                    mX = position.x;
                    mY = position.y;
                    mDeg = position.deg;
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
