package com.wztlei.tanktrouble.battle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.wztlei.tanktrouble.R;

public class PlayerTank {

    private Bitmap[] mBitmapsByAngle;
    private float mX, mY, mAngle;
    private int mMapHeight, mMapWidth;

    //private static final String TAG = "PlayerTank.java";
    private static final double SPEED = 0.1;
    private static final double BITMAP_SCALE = 0.8;
    private static final int MAX_ANGLE = 360;

    /**
     * Constructor function for the Player Tank class.
     *
     * @param context       the context in which the player tank is instantiated
     * @param isUserTank    stores whether it is a user's or an opponent's tank
     */
    PlayerTank(Context context, boolean isUserTank) {

        Bitmap mResourceBitmap;

        // Get the blue tank bitmap if it is the user tank or
        // get the red tank bitmap if it is an opponent's tank
        if (isUserTank) {
            mResourceBitmap = BitmapFactory.decodeResource
                    (context.getResources(), R.drawable.blue_tank);
            mResourceBitmap = Bitmap.createScaledBitmap(mResourceBitmap,
                    (int) (mResourceBitmap.getWidth()*BITMAP_SCALE),
                    (int) (mResourceBitmap.getHeight()*BITMAP_SCALE), false);
        } else {
            mResourceBitmap = BitmapFactory.decodeResource
                    (context.getResources(), R.drawable.red_tank);
            mResourceBitmap = Bitmap.createScaledBitmap(mResourceBitmap,
                    (int) (mResourceBitmap.getWidth()*BITMAP_SCALE),
                    (int) (mResourceBitmap.getHeight()*BITMAP_SCALE), false);
        }

        // I have to store the rotated bitmaps in advance since
        // for some reason I cannot rotate a bitmap outside of the constructor
        Matrix matrix = new Matrix();
        int bitmapWidth = mResourceBitmap.getWidth();
        int bitmapHeight =  mResourceBitmap.getHeight();
        mBitmapsByAngle = new Bitmap[MAX_ANGLE+1];

        // Store all the rotated bitmaps, one for each angle from 0 to 360 degrees
        for (int angle = 0; angle <= MAX_ANGLE; angle++) {
            matrix.setRotate(angle);
            mBitmapsByAngle[angle] = Bitmap.createBitmap(mResourceBitmap, 0, 0,
                    bitmapWidth, bitmapHeight, matrix, false);
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

        if (mAngle >= 0) {
            canvas.drawBitmap(mBitmapsByAngle[(int) mAngle], mX, mY, null);
        } else {
            canvas.drawBitmap(mBitmapsByAngle[(int) (360 + mAngle)], mX, mY, null);
        }
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
