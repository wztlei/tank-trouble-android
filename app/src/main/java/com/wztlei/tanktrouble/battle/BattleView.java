package com.wztlei.tanktrouble.battle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wztlei.tanktrouble.NetworkChangeReceiver;
import com.wztlei.tanktrouble.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@SuppressLint("ViewConstructor")
public class BattleView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private Activity mActivity;
    private Bitmap mFireBitmap, mFirePressedBitmap;
    private Canvas mCanvas;
    private BattleThread mBattleThread;
    private DocumentReference mUserDocument;
    private FirebaseFirestore mFirestore;
    private PlayerTank mUserTank;
    private PlayerTank mOpponentTank;
    private int mJoystickBaseCenterX, mJoystickBaseCenterY;
    private int mX, mY, mAngle;
    private int mFireButtonOffsetX, mFireButtonOffsetY;
    private int mJoystickX, mJoystickY;
    private int mJoystickPointerId, mFireButtonPointerId;
    private boolean mFireButtonPressed;


    private static final String TAG = "WL: BattleView.java";
    private static final int JOYSTICK_BASE_RADIUS = 165;
    private static final int JOYSTICK_THRESHOLD_RADIUS = (int) (JOYSTICK_BASE_RADIUS * 1.4);
    private static final int CONTROL_X_MARGIN = 110;
    private static final int FIRE_BUTTON_DIAMETER = 200;
    private static final int FIRE_BUTTON_PRESSED_DIAMETER = 150;
    private static final String USERS_KEY = "users";
    private static final String USER_ID_KEY = "userId";
    private static final String X_FIELD = "x";
    private static final String Y_FIELD = "y";
    private static final String ANGLE_FIELD = "angle";

    /**
     * Constructor function for the Battle View class.
     *
     * @param activity the activity in which the battle view is instantiated
     */
    public BattleView(Activity activity) {
        super(activity);
        mActivity = activity;
        Log.i(TAG, "BattleView");

        // Callback allows us to intercept events
        getHolder().addCallback(this);

        mBattleThread = new BattleThread(getHolder(), this);
        setFocusable(true);
        setGraphicsData();
        //ZTE A1R Axon - Me
        //screen-width=1080
        //screen-height=1920

        // Samsung - Suyog
        //screen-width=540
        //screen-height=960
        setOnTouchListener(this);

        // Get the user document from Firestore
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String mUserId = sharedPref.getString(USER_ID_KEY, "");
        mFirestore = FirebaseFirestore.getInstance();
        mUserDocument = mFirestore.collection(USERS_KEY).document(mUserId);
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
     * Sets the member variables of the Battle View class that are related to graphics.
     */
    private void setGraphicsData() {
        // Get the height and width of the device in pixels
        int mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int mScreenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        Log.d(TAG, "screen-width=" + mScreenWidth);
        Log.d(TAG, "screen-height=" + mScreenHeight);

        // Get the two bitmaps for the fire button (bigger = unpressed; smaller = pressed)
        mFireBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource
                        (mActivity.getResources(), R.drawable.crosshairs),
                FIRE_BUTTON_DIAMETER, FIRE_BUTTON_DIAMETER, false);
        mFirePressedBitmap = Bitmap.createScaledBitmap(mFireBitmap,
                FIRE_BUTTON_PRESSED_DIAMETER, FIRE_BUTTON_PRESSED_DIAMETER, false);

        // Set the center of the joystick base, the center of the fire button,
        // and the starting touch events
        mJoystickBaseCenterX = CONTROL_X_MARGIN + JOYSTICK_BASE_RADIUS;
        mJoystickBaseCenterY = mScreenHeight - 200 - JOYSTICK_BASE_RADIUS;
        mJoystickX = mJoystickBaseCenterX;
        mJoystickY = mJoystickBaseCenterY;
        mFireButtonOffsetX = (int)(mScreenWidth - CONTROL_X_MARGIN*1.5 - FIRE_BUTTON_DIAMETER);
        mFireButtonOffsetY =  mJoystickBaseCenterY - FIRE_BUTTON_DIAMETER/2;
        mJoystickPointerId = MotionEvent.INVALID_POINTER_ID;
        mFireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
    }

    /**
     * Updates the location and angle of the tank based on the most recent touch events
     * by the user.
     */
    public void update() {
        // Determine the displacement of the joystick in the x and y axes
        int deltaX = mJoystickX-mJoystickBaseCenterX;
        int deltaY = mJoystickY-mJoystickBaseCenterY;

        // Only move and rotate the tank if the user has moved the joystick
        if (calcDistance(deltaX, deltaY) <= JOYSTICK_THRESHOLD_RADIUS) {
            float velocityX = deltaX/JOYSTICK_THRESHOLD_RADIUS;
            float velocityY = deltaY/JOYSTICK_THRESHOLD_RADIUS;

            mUserTank.moveAndRotate(velocityX, velocityY, calcAngle(deltaX, deltaY));

            mX = (int) mUserTank.getX();
            mY = (int) mUserTank.getY();
            mAngle = (int) mUserTank.getAngle();
        }
    }

    /**
     * Draws the joystick onto the canvas and draws the blue joystick controller based on how 
     * the user is moving the joystick.
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
     * Draws a red crosshairs bitmap on the canvas which is the fire button for the user.
     *
     * @param canvas the canvas on which the fire button is drawn
     */
    public void drawFireButton(Canvas canvas) {

        // Determine whether to draw the smaller pressed fire button bitmap
        // or the larger unpressed fire button bitmap
        if (mFireButtonPressed) {
            double x = mFireButtonOffsetX + (FIRE_BUTTON_DIAMETER-FIRE_BUTTON_PRESSED_DIAMETER)/2;
            double y = mJoystickBaseCenterY - FIRE_BUTTON_PRESSED_DIAMETER/2;
            canvas.drawBitmap(mFirePressedBitmap, (int) x, (int) y, null);

        } else {
            canvas.drawBitmap(mFireBitmap, mFireButtonOffsetX, mFireButtonOffsetY, null);
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
     * Intercepts touch events to get user input for joystick and fire button control.
     * 
     * @param   view        the view from which the listener is called
     * @param   motionEvent the type of touch motion detected
     * @return              true if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        // Get the pointer index and ID from the motion event object
        int pointerIndex = motionEvent.getActionIndex();
        int pointerId = motionEvent.getPointerId(pointerIndex);

        // Get the pointer's x and y
        int pointerX = (int) motionEvent.getX(pointerIndex);
        int pointerY = (int) motionEvent.getY(pointerIndex);

        // Get masked (not specific to a pointer) action
        int action = motionEvent.getActionMasked();
        
        // Determine the type of touch event action
        switch (action) {
            // User has put a new pointer down on the screen
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Only update the joystick data if there is no pointer id
                // associated with the joystick touch events yet
                if (mJoystickPointerId == MotionEvent.INVALID_POINTER_ID) {
                    updateJoystickData(pointerX, pointerY, pointerId);
                }

                // Only update the fire button data if there is no pointer id
                // associated with the fire button touch events yet
                if (mFireButtonPointerId == MotionEvent.INVALID_POINTER_ID) {
                    updateFireButtonData(pointerX, pointerY, pointerId);
                }
                break;

            // User has moved a pointer on the screen
            case MotionEvent.ACTION_MOVE:
                // Go through all the pointers since getActionIndex() will always be 0 
                // because MotionEvent.ACTION_MOVE is only interested in the primary pointer
                for (int index = 0; index < motionEvent.getPointerCount(); index++) {
                    int id = motionEvent.getPointerId(index);
                    int x = (int) motionEvent.getX(index);
                    int y = (int) motionEvent.getY(index);

                    // Determine if the moving pointer is a joystick or fire button pointer
                    // If so, update the joystick or fire button data accordingly
                    if (id == mJoystickPointerId) {
                        updateJoystickData(x, y, id);
                    } else if (id == mFireButtonPointerId){
                        updateFireButtonData(x, y, id);
                    }
                }
                break;

            // User has removed a pointer up and off the screen
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                // Determine if  the removed pointer is a joystick or fire button pointer
                // If so, reset the joystick or fire button data accordingly to default values
                if (pointerId == mJoystickPointerId) {
                    mJoystickX = mJoystickBaseCenterX;
                    mJoystickY = mJoystickBaseCenterY;
                    mJoystickPointerId = MotionEvent.INVALID_POINTER_ID;
                } else if (pointerId == mFireButtonPointerId){
                    mFireButtonPressed = false;
                    mFireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
                }
                break;

            default:
                break;
        }

        return true;
    }

    /**
     * Updates the joystick's coordinate and pointer ID data using values retrieved from a touch
     * event. It changes the joystick's coordinates and pointer ID when the user moves the joystick
     * and resets this data when the user touches outside the joystick threshold boundaries.
     *
     * @param pointerX  the x coordinate of the touch event
     * @param pointerY  the y coordinate of the touch event
     * @param pointerId the pointer ID of the touch event
     */
    private void updateJoystickData(int pointerX, int pointerY, int pointerId) {
        // Calculate the displacement from the center of the joystick
        int deltaX = pointerX - mJoystickBaseCenterX;
        int deltaY = pointerY - mJoystickBaseCenterY;
        int displacement = calcDistance(deltaX, deltaY);

        // Determine whether the user has touched sufficiently close to the joystick center
        if (displacement <= JOYSTICK_THRESHOLD_RADIUS) {
            mJoystickX = pointerX;
            mJoystickY = pointerY;
            mJoystickPointerId = pointerId;
        } else {
            mJoystickX = mJoystickBaseCenterX;
            mJoystickY = mJoystickBaseCenterY;
        }
    }

    /**
     * Updates a boolean value storing whether the fire button has been pressed and the ID for the
     * pointer that has pressed the button using values retrieved from a touch event.
     *
     * @param pointerX  the x coordinate of the touch event
     * @param pointerY  the y coordinate of the touch event
     * @param pointerId the pointer ID of the touch event
     */
    private void updateFireButtonData(int pointerX, int pointerY, int pointerId) {
        // Calculate the displacement from the center of the fire button
        int deltaX = pointerX - (mFireButtonOffsetX + FIRE_BUTTON_DIAMETER/2);
        int deltaY = pointerY - (mFireButtonOffsetY + FIRE_BUTTON_DIAMETER/2);
        int displacement = calcDistance(deltaX, deltaY);

        // Determine whether the user has touched close enough to the center of the fire button
        if (displacement <= FIRE_BUTTON_DIAMETER) {
            if (!mFireButtonPressed) {
                Log.d(TAG, "Projectile fired at x=" + mX + " y=" + mY +
                        " mAngle=" + mAngle + " degrees");
                updateFirestoreFireData(X_FIELD, mX);
            }

            mFireButtonPressed = true;
            mFireButtonPointerId = pointerId;
        } else {
            mFireButtonPressed = false;
            mFireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
        }
    }


    private void updateFirestoreFireData(String field, final float data) {
        Random random = new Random();
        final int rand = random.nextInt(1000);

        mUserDocument.update(field, data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "updateFirestoreFireData data=" + data);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });


        // Add a new document with a generated id.
        Map<String, Object> map = new HashMap<>();
        map.put("testX", mX);
        map.put("testY", mY);

        Log.d(TAG, "add to testUsers");

        mFirestore.collection("testUsers")
                .add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "new doc with x=" + mX + " y=" + mY);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
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
