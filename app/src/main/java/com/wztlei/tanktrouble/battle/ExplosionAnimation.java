package com.wztlei.tanktrouble.battle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.tank.Tank;

public class ExplosionAnimation {
    
    private long mPrevFrameTime;
    private int mFrameIndex;
    private int mX;
    private int mY;

    private static Bitmap[] sExplosionBitmaps;
    private static int sExplosionWidth;
    private static int sExplosionHeight;

    private static final float EXPLOSION_WIDTH_CONST = (float) 205/1080;
    private static final float EXPLOSION_HEIGHT_CONST = (float) 139/1080;
    private static final int EXPLOSION_FRAME_DURATION = 80;
    
    /**
     * Initializes the array of bitmaps that displays an explosion.
     *
     * @param activity  the activity in which the activity is created
     */
    public static void initialize(Activity activity) {
        // Get the width and height of each explosion image
        sExplosionWidth = UserUtils.scaleGraphicsInt(EXPLOSION_WIDTH_CONST);
        sExplosionHeight = UserUtils.scaleGraphicsInt(EXPLOSION_HEIGHT_CONST);

        // Fill the explosion bitmap array with bitmaps
        sExplosionBitmaps = new Bitmap[6];
        sExplosionBitmaps[0] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion0),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[1] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion1),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[2] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion2),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[3] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion3),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[4] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion4),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[5] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion5),
                sExplosionWidth, sExplosionHeight, false);
    }

    /**
     * Constructor function for an explosion frame.
     */
    ExplosionAnimation(Tank tank) {
        PointF tankCenter = tank.getCenter();

        mPrevFrameTime = System.currentTimeMillis();
        mFrameIndex = 0;
        mX = (int) tankCenter.x - sExplosionWidth/2;
        mY = (int) tankCenter.y - sExplosionHeight/2;
    }

    /**
     * Returns true if this frame can be removed.
     *
     * @return  true if we have reached the last frame to be drawn
     */
    public boolean isRemovable() {
        return mFrameIndex >= sExplosionBitmaps.length;
    }

    /**
     * Draws an explosion frame onto a canvas and increases the frame index when
     * the current frame has existed past the threshold duration.
     *
     * @param canvas    the canvas on which the explosion frame is drawn
     */
    public void draw(Canvas canvas) {
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - mPrevFrameTime;

        // Increment the frame when necessary
        if (deltaTime > EXPLOSION_FRAME_DURATION) {
            mPrevFrameTime = nowTime;
            mFrameIndex++;
        } 

        // Draw the frame if the index is valid
        if (mFrameIndex < sExplosionBitmaps.length) {
            canvas.drawBitmap(sExplosionBitmaps[mFrameIndex], mX, mY, null);
        }
    }
}
