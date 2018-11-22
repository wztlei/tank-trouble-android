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
import com.wztlei.tanktrouble.Globals;
import com.wztlei.tanktrouble.R;

public class OpponentTank {
    private Bitmap mBitmap;
    private DatabaseReference mDataRef;
    private float mX, mY, mDegrees;

    private static final String TAG = "WL: UserTank";
    private static final String USERS_KEY = Globals.USERS_KEY;


    OpponentTank(Activity activity, String opponentId) {
        // Get the red tank bitmap since it is an opponent's tank
        mBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.red_tank);

        // Get the user document from Firestore
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        if (opponentId.length() > 0) {
            mDataRef = database.child(USERS_KEY).child(opponentId);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }

        mDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Position position = dataSnapshot.getValue(Position.class);

                if (position != null) {
                    mX = position.x;
                    mY = position.y;
                    mDegrees = position.deg;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

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
}
