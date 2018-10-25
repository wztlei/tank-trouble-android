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

    private BattleThread mBattleThread;
    private PlayerTank mUserTank;
    private PlayerTank mOpponentTank;
    private Activity mActivity;
    private Bitmap mFireBitmap;
    private Canvas mCanvas;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mJoystickCenterX;
    private int mJoystickCenterY;
    private int mTouchEventX;
    private int mTouchEventY;

    private static final String TAG = "WL: BattleView.java";
    private static final int FIRE_BUTTON_DIAMETER = 200;
    private static final int JOYSTICK_RADIUS = 150;

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
                        FIRE_BUTTON_DIAMETER, FIRE_BUTTON_DIAMETER, false);

        int mJoystickMargin = 100;

        mJoystickCenterX = mJoystickMargin + JOYSTICK_RADIUS;
        mJoystickCenterY = mScreenHeight - 2* mJoystickMargin - JOYSTICK_RADIUS;
        mTouchEventX = mJoystickCenterX;
        mTouchEventY = mJoystickCenterY;

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
        mUserTank.move(mTouchEventX-mJoystickCenterX, mTouchEventY-mJoystickCenterY);
    }


    public void drawJoystick(Canvas canvas, int controlX, int controlY) {

        int controlRadius = JOYSTICK_RADIUS / 2;

        Paint colors = new Paint();
        colors.setARGB(50, 50, 50, 50);
        canvas.drawCircle(mJoystickCenterX, mJoystickCenterY, JOYSTICK_RADIUS, colors);

        colors.setARGB(255, 79, 121, 255);
        canvas.drawCircle(controlX, controlY, controlRadius, colors);

    }

    public void drawFireButton(Canvas canvas) {

        int x = mScreenWidth - 100 - FIRE_BUTTON_DIAMETER;
        int y = mScreenHeight - 240 - FIRE_BUTTON_DIAMETER;

        canvas.drawBitmap(mFireBitmap, x, y, null);
    }

    @Override
    public void draw(Canvas canvas) {

        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        mUserTank.draw(canvas);
        mOpponentTank.draw(canvas);
        drawFireButton(canvas);
        drawJoystick(mCanvas, getJoystickX(), getJoystickY());

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (view.equals(this)) {
            if(motionEvent.getAction() != MotionEvent.ACTION_UP) {
                mTouchEventX = (int) motionEvent.getX();
                mTouchEventY = (int) motionEvent.getY();
            } else {
                mTouchEventX = mJoystickCenterX;
                mTouchEventY = mJoystickCenterY;
            }
        }

        return true;
    }

    public int getJoystickX() {
        int deltaX = mTouchEventX - mJoystickCenterX;
        int deltaY = mTouchEventY - mJoystickCenterY;
        int displacement = (int) Math.sqrt(deltaX*deltaX + deltaY*deltaY);

        if (displacement < JOYSTICK_RADIUS*1.5) {
            return mTouchEventX;
        } else {
            return mJoystickCenterX;
        }
    }

    public int getJoystickY() {
        int deltaX = mTouchEventX - mJoystickCenterX;
        int deltaY = mTouchEventY - mJoystickCenterY;
        int displacement = (int) Math.sqrt(deltaX*deltaX + deltaY*deltaY);

        if (displacement < JOYSTICK_RADIUS*1.5) {
            return mTouchEventY;
        } else {
            return mJoystickCenterY;
        }
    }
}
