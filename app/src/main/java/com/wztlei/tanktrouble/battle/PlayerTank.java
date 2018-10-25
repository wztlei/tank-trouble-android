package com.wztlei.tanktrouble.battle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.wztlei.tanktrouble.R;

public class PlayerTank {

    private Bitmap mBitmap;
    private float x, y;
    private String TAG = "PlayerTank.java";
    private int mScreenHeight, mScreenWidth;


    public PlayerTank(Context context, boolean isUserTank) {


        if (isUserTank) {
            mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.blue_tank);
        } else {
            mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.red_tank);
        }

        if (isUserTank) {
            x = 100;
            y = 100;
        } else {
            x = 300;
            y = 100;
        }

        mScreenHeight = Resources.getSystem().getDisplayMetrics().heightPixels - 400;
        mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, x, y, null);
    }

    public void move(int deltaX, int deltaY) {

        if ((x + deltaX >= 0) && (x + deltaX <= mScreenWidth)) {
            x += deltaX;
        }

        if ((y + deltaY >= 0) && (y + deltaY <= 1300)) {
            y += deltaY;
        }

        Log.d(TAG, x + " " + y);
    }
}
