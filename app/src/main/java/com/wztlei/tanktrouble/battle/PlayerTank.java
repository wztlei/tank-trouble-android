package com.wztlei.tanktrouble.battle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.wztlei.tanktrouble.R;

public class PlayerTank {

    private Bitmap mBitmap;
    private float mX, mY, mAngle;
    private int mMapHeight, mMapWidth;

    //private static final String TAG = "PlayerTank.java";
    private static final double SPEED = 0.1;

    /**
     * Constructor function for the Player Tank class.
     *
     * @param context       the context in which the player tank is instantiated
     * @param isUserTank    stores whether it is a user's or an opponent's tank
     */
    PlayerTank(Context context, boolean isUserTank) {

        // Get the blue tank bitmap if it is the user tank or
        // get the red tank bitmap if it is an opponent's tank
        if (isUserTank) {
            mBitmap = BitmapFactory.decodeResource
                    (context.getResources(), R.drawable.blue_tank);
        } else {
            mBitmap = BitmapFactory.decodeResource
                    (context.getResources(), R.drawable.red_tank);
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
        mMapHeight = Resources.getSystem().getDisplayMetrics().heightPixels - 400;
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

        // Set the new x and y coordinates assuming the tank can move there and the new angle
        mX += SPEED * deltaX;
        mY += SPEED * deltaY;
        mAngle = angle;

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
    }
}
