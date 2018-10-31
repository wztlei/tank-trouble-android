package com.wztlei.tanktrouble.battle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.wztlei.tanktrouble.R;

@SuppressLint("ViewConstructor")
public class BattleView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private BattleThread mBattleThread;
    private PlayerTank mUserTank;
    private PlayerTank mOpponentTank;
    private Activity mActivity;
    private Bitmap mFireBitmap;
    private Canvas mCanvas;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mJoystickBaseCenterX;
    private int mJoystickBaseCenterY;
    private int mTouchEventX;
    private int mTouchEventY;
    private int mAngle;

    //private static final String TAG = "WL: BattleView.java";
    private static final int FIRE_BUTTON_DIAMETER = 200;
    private static final int JOYSTICK_BASE_RADIUS = 150;
    private static final int JOYSTICK_THRESHOLD_RADIUS = (int) (JOYSTICK_BASE_RADIUS * 1.5);

    /**
     * Constructor function for the Battle View class.
     *
     * @param activity the activity in which the battle view is instantiated
     */
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

        // Set the center of the joystick base and the starting touch events
        mJoystickBaseCenterX = 100 + JOYSTICK_BASE_RADIUS;
        mJoystickBaseCenterY = mScreenHeight - 200 - JOYSTICK_BASE_RADIUS;
        mTouchEventX = mJoystickBaseCenterX;
        mTouchEventY = mJoystickBaseCenterY;

        setOnTouchListener(this);
    }

    public void setCanvas(Canvas canvas) {
        this.mCanvas = canvas;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

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

    /**
     * Updates the location and angle of the tank based on the most recent touch events
     * by the user.
     */
    public void update() {
        int deltaX = mTouchEventX-mJoystickBaseCenterX;
        int deltaY = mTouchEventY-mJoystickBaseCenterY;

        // Only change the angle of the tank if the tank has actually moved
        if (deltaX != 0 || deltaY != 0) {
            mAngle = calcAngle(deltaX, deltaY);
        }

        // Only move and rotate the tank if the user has moved the joystick
        if (calcDistance(deltaX, deltaY) <= JOYSTICK_THRESHOLD_RADIUS) {
            mUserTank.moveAndRotate(deltaX, deltaY, mAngle);
        }
    }

    /**
     *
     * @param canvas    the canvas on which the joystick is drawn
     * @param joystickX the x-coordinate of where the joystick controller should be drawn
     * @param joystickY the y-coordinate of where the joystick controller should be drawn
     */
    public void drawJoystick(Canvas canvas, int joystickX, int joystickY) {

        int controlRadius = JOYSTICK_BASE_RADIUS / 2;

        // Draw the base of the joystick
        Paint colors = new Paint();
        colors.setARGB(50, 50, 50, 50);
        canvas.drawCircle(mJoystickBaseCenterX, mJoystickBaseCenterY, 
                JOYSTICK_BASE_RADIUS, colors);

        // Draw the actual joystick controller of the joystick
        colors.setARGB(255, 79, 121, 255);
        canvas.drawCircle(joystickX, joystickY, controlRadius, colors);
    }

    /**
     * Draws a red crosshairs bitmap on the canvas which is the "fire" button for the user.
     *
     * @param canvas the canvas on which the fire button is drawn
     */
    public void drawFireButton(Canvas canvas) {
        int x = mScreenWidth - 100 - FIRE_BUTTON_DIAMETER;
        int y = mScreenHeight - 240 - FIRE_BUTTON_DIAMETER;
        canvas.drawBitmap(mFireBitmap, x, y, null);
    }

    /**
     * Draws the game's graphics onto a canvas.
     *
     * @param canvas the canvas on which the game graphics are drawn
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        mUserTank.draw(canvas);
        mOpponentTank.draw(canvas);
        drawFireButton(canvas);
        drawJoystick(mCanvas, getJoystickX(), getJoystickY());
    }

    /**
     * Detects touch events from the user and sets mTouchEventX and mTouchEventY to allow the
     * user to control their tank. It also resets mTouchEventX and mTouchEventY so that the
     * joystick returns to its default position once the user lifts their finger off the screen.
     *
     * @param   view        the view from which the listener is called
     * @param   motionEvent the type of touch motion detected
     * @return              true if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.equals(this)) {
            // Check whether the user has lifted their finger from the screen
            if(motionEvent.getAction() != MotionEvent.ACTION_UP) {
                mTouchEventX = (int) motionEvent.getX();
                mTouchEventY = (int) motionEvent.getY();
            } else {
                mTouchEventX = mJoystickBaseCenterX;
                mTouchEventY = mJoystickBaseCenterY;
            }
        }

        return true;
    }

    /**
     * Calculates the x-coordinate for where the blue joystick control circle should be drawn.
     * It only moves the joystick circle if the user has touched within 1.5x the radius
     * of the base. Otherwise, it resets the joystick to the center of the base.
     * 
     * @return  the x-coordinate of where the joystick control should be drawn
     */
    private int getJoystickX() {
        
        // Calculate the displacement from the center of the joystick 
        int deltaX = mTouchEventX - mJoystickBaseCenterX;
        int deltaY = mTouchEventY - mJoystickBaseCenterY;
        int displacement = (int) Math.sqrt(deltaX*deltaX + deltaY*deltaY);

        // Check whether the user has touched sufficiently close to the joystick center
        if (displacement < 1.5*JOYSTICK_BASE_RADIUS) {
            return mTouchEventX;
        } else {
            return mJoystickBaseCenterX;
        }
    }
    
    /**
     * Returns the y-coordinate for where the blue joystick control circle should be drawn.
     * It only moves the joystick circle if the user has touched within 1.5x the radius
     * of the base. Otherwise, it resets the joystick to the center of the base.
     *
     * @return  the y-coordinate for where the joystick control should be drawn
     */
    private int getJoystickY() {

        // Calculate the displacement from the center of the joystick
        int deltaX = mTouchEventX - mJoystickBaseCenterX;
        int deltaY = mTouchEventY - mJoystickBaseCenterY;
        int displacement = calcDistance(deltaX, deltaY);

        // Check whether the user has touched sufficiently close to the joystick center
        if (displacement < JOYSTICK_BASE_RADIUS*1.5) {
            return mTouchEventY;
        } else {
            return mJoystickBaseCenterY;
        }
    }


    /**
     * Calculates the angle in degrees about the origin, given a displacement in the x-axis
     * and a displacement in the y-axis.
     * 
     * @param   x   the displacement in the x-axis
     * @param   y   the displacement in the y-axis
     * @return      the angle in degrees about the origin
     */
    public int calcAngle(int x, int y) {
        // Calculates an angle between 0 and 180 degrees
        double displacement = calcDistance(x, y);
        double angle = Math.acos(x/displacement);

        // Check whether the point (x, y) is above or below the x-axis
        // to convert the angle to between 0 and 360 degrees
        if (y >= 0) {
            return (int) Math.toDegrees(angle);
        } else {
            return (int) -Math.toDegrees(angle);
        }
    }

    /**
     * Calculates the Euclidean distance between two points, given a displacement in the x-axis
     * and a displacement in the y-axis using the Pythagorean theorem.
     *
     * @param   x   the displacement in the x-axis
     * @param   y   the displacement in the y-axis
     * @return      the Euclidean distance calculated using the Pythagorean theorem
     */
    private int calcDistance (int x, int y) {
        return (int) Math.sqrt(x*x + y*y);
    }
}
