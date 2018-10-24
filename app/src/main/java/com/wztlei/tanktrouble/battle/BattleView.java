package com.wztlei.tanktrouble.battle;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;

import com.wztlei.tanktrouble.R;

public class BattleView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private Canvas mCanvas;
    private BattleThread mBattleThread;
    private PlayerTank mUserTank;
    private PlayerTank mOpponentTank;
    private Activity mActivity;
    private int mScreenHeight;
    private int mScreenWidth;
    private Bitmap mFireBitmap;
    private int mFireDiameter = 200;
    private int mJoystickRadius = 150;
    private int mJoystickMargin = 100;
    private int mJoystickCenterX;
    private int mJoystickCenterY;
    private static String sTag = "BattleView";

    public BattleView(Activity activity) {
        super(activity);

        mActivity = activity;

        // Callback allows us to intercept events
        getHolder().addCallback(this);

        mBattleThread = new BattleThread(getHolder(), this);
        setFocusable(true);

        // Get the height and width of the device in pixels
        mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        mScreenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        mFireBitmap = Bitmap.createScaledBitmap
                (BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.crosshairs),
                        mFireDiameter, mFireDiameter, false);

        mJoystickCenterX = mJoystickMargin + mJoystickRadius;
        mJoystickCenterY = mScreenHeight - 2*mJoystickMargin - mJoystickRadius;

        setOnTouchListener(this);
    }

    public void setCanvas(Canvas canvas) {
        this.mCanvas = canvas;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mUserTank = new PlayerTank(mActivity, true);
        mOpponentTank = new PlayerTank(mActivity, false);

        mBattleThread.setRunning(true);
        mBattleThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;

        while (retry) {
            try {
                mBattleThread.setRunning(false);
                mBattleThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            retry = false;
        }
    }

    public void update() {
        mUserTank.move();
    }

    public void drawJoystick(Canvas canvas) {

        int controlRadius = mJoystickRadius / 2;

        Paint colors = new Paint();
        colors.setARGB(50, 50, 50, 50);
        canvas.drawCircle(mJoystickCenterX, mJoystickCenterY, mJoystickRadius, colors);

        colors.setARGB(255, 79, 121, 255);
        canvas.drawCircle(mJoystickCenterX, mJoystickCenterY, controlRadius, colors);

    }

    public void drawJoystick(Canvas canvas, int controlX, int controlY) {

        int controlRadius = mJoystickRadius / 2;

        Paint colors = new Paint();
        colors.setARGB(50, 50, 50, 50);
        canvas.drawCircle(mJoystickCenterX, mJoystickCenterY, mJoystickRadius, colors);

        colors.setARGB(255, 79, 121, 255);
        canvas.drawCircle(controlX, controlY, controlRadius, colors);

    }

    public void drawFireButton(Canvas canvas) {

        int x = mScreenWidth - 100 - mFireDiameter;
        int y = mScreenHeight - 240 - mFireDiameter;

        canvas.drawBitmap(mFireBitmap, x, y, null);
    }

    @Override
    public void draw(Canvas canvas) {

        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        mUserTank.draw(canvas);
        mOpponentTank.draw(canvas);
        drawFireButton(canvas);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (view.equals(this)) {
            /*if(motionEvent.getAction() != MotionEvent.ACTION_UP) {
                drawJoystick(mCanvas, (int) motionEvent.getX(), (int) motionEvent.getY());
                Log.d(sTag, "move joystick");

            } else {
                drawJoystick(mCanvas, mJoystickCenterX, mJoystickCenterY);
            }*/

            Log.d(sTag, "onTouch");
        }

        return true;
    }
}
