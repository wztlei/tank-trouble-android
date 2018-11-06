package com.wztlei.tanktrouble.battle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
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
    private Bitmap mFireBitmap, mFirePressedBitmap;
    private Canvas mCanvas;
    private int mScreenWidth, mScreenHeight;
    private int mJoystickBaseCenterX, mJoystickBaseCenterY;
    private int mAngle;
    private int mFireButtonCenterX, mFireButtonCenterY;
    private int mJoystickX, mJoystickY;
    private int mJoystickPointerId, mFireButtonPointerId;
    private boolean mFireButtonPressed;
    private int mFireButtonDiameter = 200;
    private int mFireButtonPressedDiameter = 150;


    private static final String TAG = "WL: BattleView.java";

    private static final int JOYSTICK_BASE_RADIUS = 150;
    private static final int JOYSTICK_THRESHOLD_RADIUS = (int) (JOYSTICK_BASE_RADIUS * 1.5);
    private static final int CONTROL_X_MARGIN = 100;

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

        // Get the two bitmaps for the fire button (bigger = unpressed; smaller = pressed)
        mFireBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource
                (mActivity.getResources(), R.drawable.crosshairs), 200, 200, false);
        mFirePressedBitmap = Bitmap.createScaledBitmap(mFireBitmap, 150, 150, false);

        // Set the center of the joystick base, the center of the fire button,
        // and the starting touch events
        mJoystickBaseCenterX = CONTROL_X_MARGIN + JOYSTICK_BASE_RADIUS;
        mJoystickBaseCenterY = mScreenHeight - 200 - JOYSTICK_BASE_RADIUS;
        mJoystickX = mJoystickBaseCenterX;
        mJoystickY = mJoystickBaseCenterY;
        mFireButtonCenterX = mScreenWidth - CONTROL_X_MARGIN - mFireButtonDiameter/2;
        mFireButtonCenterY =  mScreenHeight - 240 - mFireButtonDiameter/2;
        //mFireButtonCenterX = 100;
        //mFireButtonCenterY = 100;
        mJoystickPointerId = -1;

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
        int deltaX = mJoystickX-mJoystickBaseCenterX;
        int deltaY = mJoystickY-mJoystickBaseCenterY;


        // Only move and rotate the tank if the user has moved the joystick
        if (calcDistance(deltaX, deltaY) <= JOYSTICK_THRESHOLD_RADIUS) {

            // Only change the angle of the tank if the tank has actually moved
            if (deltaX != 0 || deltaY != 0) {
                mAngle = calcAngle(deltaX, deltaY);
            }

            mUserTank.moveAndRotate(deltaX, deltaY, mAngle);
        }
    }

    /**
     *
     * @param canvas    the canvas on which the joystick is drawn
     */
    public void drawJoystick(Canvas canvas) {

        int controlRadius = JOYSTICK_BASE_RADIUS / 2;

        // Draw the base of the joystick
        Paint colors = new Paint();
        colors.setARGB(50, 50, 50, 50);
        canvas.drawCircle(mJoystickBaseCenterX, mJoystickBaseCenterY, 
                JOYSTICK_BASE_RADIUS, colors);

        // Draw the actual joystick controller of the joystick
        colors.setARGB(255, 79, 121, 255);
        canvas.drawCircle(mJoystickX, mJoystickY, controlRadius, colors);
    }

    /**
     * Draws a red crosshairs bitmap on the canvas which is the "fire" button for the user.
     *
     * @param canvas the canvas on which the fire button is drawn
     */
    public void drawFireButton(Canvas canvas) {

        // Determine whether to draw the smaller pressed fire button bitmap
        // or the larger unpressed fire button bitmap
        if (mFireButtonPressed) {
            double x = mScreenWidth - 100 - mFireButtonPressedDiameter
                    - 0.5*(mFireButtonDiameter-mFireButtonPressedDiameter);
            double y = mScreenHeight - 240 - mFireButtonPressedDiameter
                    - 0.5*(mFireButtonDiameter-mFireButtonPressedDiameter);

            canvas.drawBitmap(mFirePressedBitmap, (int) x, (int) y, null);
            //canvas.drawBitmap(mFirePressedBitmap, 100, 100, null);
        } else {
            int x = mScreenWidth - 100 - mFireButtonDiameter;
            int y = mScreenHeight - 240 - mFireButtonDiameter;
            canvas.drawBitmap(mFireBitmap, x, y, null);
           // canvas.drawBitmap(mFireBitmap, 100, 100, null);

        }
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
        drawJoystick(mCanvas);
    }

    /**
     *
     * @param   view        the view from which the listener is called
     * @param   motionEvent the type of touch motion detected
     * @return              true if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.equals(this)) {



            // Get the pointer index and ID from the motion event object
            int pointerIndex = motionEvent.getActionIndex();
            int pointerId = motionEvent.getPointerId(pointerIndex);

            // Get the pointer's x and y
            int pointerX = (int) motionEvent.getX(pointerIndex);
            int pointerY = (int) motionEvent.getY(pointerIndex);

            // Get masked (not specific to a pointer) action
            int action = motionEvent.getActionMasked();

            //
            switch (action) {

                // User has pressed down on the screen so there is a new pointer on the screen
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (mJoystickPointerId == -1) {
                        setJoystickInfo(pointerX, pointerY, pointerId);
                    }

                    if (mFireButtonPointerId == -1) {
                        setFireButtonInfo(pointerX, pointerY, pointerId);
                    }
                    break;
                // User has moved their finger on the screen
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "pointerX=" + pointerX);
                    if (pointerId == mJoystickPointerId) {
                        setJoystickInfo(pointerX, pointerY, pointerId);
                    } else if (pointerId == mFireButtonPointerId){
                        setFireButtonInfo(pointerX, pointerY, pointerId);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (pointerId == mJoystickPointerId) {
                        mJoystickX = mJoystickBaseCenterX;
                        mJoystickY = mJoystickBaseCenterY;
                        mJoystickPointerId = -1;
                    } else if (pointerId == mFireButtonPointerId){
                        mFireButtonPressed = false;
                        mFireButtonPointerId = -1;
                    }

                    break;

            }
        }

        return true;
    }

    
    /**
     * Returns the y-coordinate for where the blue joystick control circle should be drawn.
     * It only moves the joystick circle if the user has touched within 1.5x the radius
     * of the base. Otherwise, it resets the joystick to the center of the base.
     */
    private void setJoystickInfo(int touchEventX, int touchEventY, int pointerId) {

        // Calculate the displacement from the center of the joystick
        int deltaX = touchEventX - mJoystickBaseCenterX;
        int deltaY = touchEventY - mJoystickBaseCenterY;
        int displacement = calcDistance(deltaX, deltaY);

        // Check whether the user has touched sufficiently close to the joystick center
        if (displacement <= JOYSTICK_THRESHOLD_RADIUS) {
            mJoystickX = touchEventX;
            mJoystickY = touchEventY;
            mJoystickPointerId = pointerId;
        } else {
            mJoystickX = mJoystickBaseCenterX;
            mJoystickY = mJoystickBaseCenterY;
            mJoystickPointerId = -1;
        }
    }

    private void setFireButtonInfo(int touchEventX, int touchEventY, int pointerId) {
        // Get the displacement in the x and y direction of the
        // most recent touch event from the fire button's center
        int deltaX = touchEventX - mFireButtonCenterX;
        int deltaY = touchEventY - mFireButtonCenterY;

        // Determine whether to draw the smaller pressed fire button bitmap
        // or the larger unpressed fire button bitmap
        if (calcDistance(deltaX, deltaY) <= mFireButtonDiameter) {
            mFireButtonPressed = true;
            mFireButtonPointerId = pointerId;
        } else {
            mFireButtonPressed = false;
            mFireButtonPointerId = -1;
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
