package com.wztlei.tanktrouble.battle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.wztlei.tanktrouble.R;

public class PlayerTank {

    private Bitmap mBitmap;
    private float mX, mY, mAngle;
    private int mMapHeight, mMapWidth;

    private static final String TAG = "PlayerTank.java";
    private static final double SPEED = 0.1;
    private static final double BITMAP_SCALE = 0.5;


    PlayerTank(Context context, boolean isUserTank) {

        if (isUserTank) {
            mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.blue_tank);
            mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) (mBitmap.getWidth()*BITMAP_SCALE),
                    (int) (mBitmap.getHeight()*BITMAP_SCALE), false);
        } else {
            mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.red_tank);
            mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) (mBitmap.getWidth()*BITMAP_SCALE),
                    (int) (mBitmap.getHeight()*BITMAP_SCALE), false);
        }

        if (isUserTank) {
            mX = 100;
            mY = 100;
        } else {
            mX = 300;
            mY = 100;
        }

        mMapHeight = Resources.getSystem().getDisplayMetrics().heightPixels - 400;
        mMapWidth = Resources.getSystem().getDisplayMetrics().widthPixels - 26;
    }

    public void draw(Canvas canvas) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        /*Bitmap bmpRotate = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(),
                matrix, true);
        canvas.drawBitmap(bmpRotate, matrix, null);*/

        canvas.drawBitmap(mBitmap, mX, mY, null);
    }

    public void moveAndRotate(int deltaX, int deltaY, float degrees) {

        mX += SPEED * deltaX;
        mY += SPEED * deltaY;


        if (mX < 0) {
            mX = 0;
        } else if (mX > mMapWidth) {
            mX = mMapWidth;
        }

        if (mY < 0) {
            mY = 0;
        } else if (mY > mMapHeight) {
            mY = mMapHeight;
        }

        mAngle = degrees;

        Log.d(TAG, mX + " " + mY);
    }
}
