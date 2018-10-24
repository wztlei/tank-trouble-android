package com.wztlei.tanktrouble.battle;

import android.content.Context;
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
    private int x, y;
    private String sTag = "PlayerTank";


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


    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, x, y, null);
    }

    public void move() {
        x += 2;
        y += 1;
        Log.d(sTag, Integer.toString(x));
    }
}
