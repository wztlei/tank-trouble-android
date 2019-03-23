package com.wztlei.tanktrouble.tank;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

import com.wztlei.tanktrouble.R;

public enum TankColor {
    BLUE,
    RED,
    GREEN,
    ORANGE;

    /**
     * Returns the paint with the appropriate RGB color value.
     *
     * @return  the RGB color value stored in a Paint object
     */
    public Paint getPaint() {
        Paint paint = new Paint();

        switch (this) {
            case BLUE:
                paint.setARGB(255, 79, 121, 255);
                break;
            case RED:
                paint.setARGB(255, 255, 79, 79);
                break;
            case GREEN:
                paint.setARGB(255, 0, 176, 80);
                break;
            case ORANGE:
                paint.setARGB(255, 255, 158, 1);
                break;
        }

        return paint;
    }

    /**
     * Returns the tank bitmap by decoding the resource file with the tank of the desired color.
     *
     * @param activity  the activity in which this method is called
     * @return          a bitmap of the tank with the desired color
     */
    public Bitmap getTankBitmap(Activity activity) {
        Bitmap b = null;

        switch (this) {
            case BLUE:
                b = BitmapFactory.decodeResource(activity.getResources(), R.drawable.blue_tank);
                break;
            case RED:
                b = BitmapFactory.decodeResource(activity.getResources(), R.drawable.red_tank);
                break;
            case GREEN:
                b = BitmapFactory.decodeResource(activity.getResources(), R.drawable.green_tank);
                break;
            case ORANGE:
                b = BitmapFactory.decodeResource(activity.getResources(), R.drawable.orange_tank);
                break;
        }

        return b;
    }

    public int getIndex() {
        int index = -1;

        switch (this) {
            case BLUE:
                index = 0;
                break;
            case RED:
                index = 1;
                break;
            case GREEN:
                index = 2;
                break;
            case ORANGE:
                index = 3;
                break;
        }

        return index;
    }
}
