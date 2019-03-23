package com.wztlei.tanktrouble.battle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.map.MapUtils;
import com.wztlei.tanktrouble.cannonball.Cannonball;
import com.wztlei.tanktrouble.cannonball.CannonballSet;
import com.wztlei.tanktrouble.tank.OpponentTank;
import com.wztlei.tanktrouble.tank.TankColor;
import com.wztlei.tanktrouble.tank.UserTank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

@SuppressLint("ViewConstructor")
public class BattleView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private Bitmap mFireBitmap, mFirePressedBitmap;
    private DatabaseReference mGameDataRef;
    private BattleThread mBattleThread;
    private UserTank mUserTank;
    private HashMap<String, OpponentTank> mOpponentTanks;
    private HashSet<ExplosionFrame> mExplosionFrames;
    private CannonballSet mUserCannonballSet;
    private long mKillingCannonball;
    private Paint mJoystickColor;
    private int mJoystickBaseCenterX, mJoystickBaseCenterY;
    private int mFireButtonOffsetX, mFireButtonOffsetY;
    private int mJoystickX, mJoystickY;
    private int mJoystickPointerId, mFireButtonPointerId;
    private int mJoystickBaseRadius, mJoystickThresholdRadius, mJoystickMaxDisplacement;
    private int mFireButtonDiameter, mFireButtonPressedDiameter;
    private int mUserDeg;
    private boolean mFireButtonPressed;

    private static final String TAG = "WL/BattleView";
    private static final float TOP_Y_CONST = Constants.MAP_TOP_Y_CONST;
    private static final float JOYSTICK_BASE_RADIUS_CONST = (float) 165/543;
    private static final float JOYSTICK_THRESHOLD_RADIUS_CONST = (float) 220/543;
    private static final float JOYSTICK_DISPLACEMENT_CONST = 0.9f;
    private static final float FIRE_BUTTON_DIAMETER_CONST = (float) 200/543;
    private static final float FIRE_BUTTON_PRESSED_DIAMETER_CONST = (float) 150/543;
    private static final float CONTROL_X_MARGIN_CONST = (float) 125/1080;

    // TODO: Change to 5 in production version
    private static final int MAX_USER_CANNONBALLS = 10;

    /**
     * Constructor function for the Battle View class.
     *
     * @param activity the activity in which the battle view is instantiated
     */
    public BattleView(Activity activity, ArrayList<String> opponentIds, String gamePin) {
        super(activity);

        UserUtils.initialize(activity);
        ExplosionFrame.initialize(activity);

        // Set up the user and opponent tanks
        if (opponentIds != null && gamePin != null) {
            mOpponentTanks = new HashMap<>();
            mGameDataRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.GAMES_KEY).child(gamePin);
            mJoystickColor = TankColor.BLUE.getPaint();
            addEnteringTanks(activity, mGameDataRef);

            for (String opponentId : opponentIds) {
                removeExitingTanks(mGameDataRef, opponentId);
            }
        } else {
            mUserTank = new UserTank(activity, TankColor.BLUE);
            mJoystickColor = TankColor.BLUE.getPaint();
        }

        // Set up the cannonball and explosion data
        // TODO: Move this cannonball set to the UserTank.java class
        mUserCannonballSet = new CannonballSet();
        mKillingCannonball = 0;
        mExplosionFrames = new HashSet<>();

        // Callback allows us to intercept events
        getHolder().addCallback(this);

        mBattleThread = new BattleThread(getHolder(), this);
        setFocusable(true);
        setControlGraphicsData(activity);
        setOnTouchListener(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mBattleThread.getState() == Thread.State.NEW) {
            mBattleThread.setRunning(true);
            mBattleThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
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

        // Remove the game if necessary
        if (mGameDataRef != null) {
            mGameDataRef.child(UserUtils.getUserId()).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            removeGame();
                        }
                    });
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
        MapUtils.drawMap(canvas);
        drawFireButton(canvas);
        drawJoystick(canvas);

        // TODO: Display the score of tank kills

        // Draw all of the opponents' tanks and their cannonballs while detecting collisions
        for (OpponentTank opponentTank : mOpponentTanks.values()) {
            CannonballSet opponentCannonballs = opponentTank.getCannonballSet();

            if (mKillingCannonball == 0) {
                mKillingCannonball = opponentCannonballs.updateAndDetectUserCollision(mUserTank);
            } else {
                opponentCannonballs.updateAndDetectUserCollision(mUserTank);
            }

            opponentCannonballs.draw(canvas);
            opponentTank.draw(canvas);
        }

        // Check whether a cannonball collided with the user's tank
        if (mKillingCannonball == 0) {
            // Update and draw the user's tank
            if (mUserTank != null) {
                updateUserTank();
                mUserTank.draw(canvas);
                mUserTank.respawn();
            }

            // Update and draw the cannonballs
            mUserCannonballSet.draw(canvas);
            mKillingCannonball = mUserCannonballSet.updateAndDetectUserCollision(mUserTank);
        } else {
            // TODO: Update the collision status in Firebase
            // TODO: Make tank appear after x seconds
            // TODO: Increment the score and display it
            // Update and draw the user's tank
            if (mUserTank != null) {
                updateUserTank();
                mUserTank.draw(canvas);
                mUserTank.kill(mKillingCannonball);
                mExplosionFrames.add(new ExplosionFrame(mUserTank));
            }

            // If a collision occurred, then do not draw the user's tank
            // Only update and draw the cannonballs
            mUserCannonballSet.updateAndDetectUserCollision(mUserTank);
            mUserCannonballSet.draw(canvas);
            mKillingCannonball = 0;

            Log.d(TAG, "mUserCollision");
        }



        // Draw all of the explosions
        for (Iterator<ExplosionFrame> iterator = mExplosionFrames.iterator();
             iterator.hasNext(); ) {
            ExplosionFrame explosionFrame = iterator.next();
            if (explosionFrame.isRemovable()) {
                iterator.remove();
            } else {
                explosionFrame.draw(canvas);
            }
        }
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
                    updateJoystickData(pointerX, pointerY, pointerId, action);
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
                        updateJoystickData(x, y, id, action);
                    } else if (mJoystickPointerId == MotionEvent.INVALID_POINTER_ID) {
                        updateJoystickData(x, y, id, action);
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
     * Detects an opponent entering the game and adds the opponent tank to the HashMap.
     *
     * @param activity      the activity of the battle view
     * @param gameDataRef   a Firebase Database reference for the game
     */
    private void addEnteringTanks(final Activity activity, DatabaseReference gameDataRef) {
        gameDataRef.addValueEventListener(new ValueEventListener() {
            @SuppressWarnings("UnnecessaryContinue")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userId = UserUtils.getUserId();
                TankColor[] tankColors = TankColor.values();
                int numColors = tankColors.length;
                int tankIndex = 0;

                // Determine if any of the children of the game has a new key
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String key = children.getKey();

                    // Add the new opponent to the game
                    if (key == null || key.equals(Constants.STARTED_KEY)
                            || tankIndex >= numColors) {
                        continue;
                    } else if (key.equals(userId) && mUserTank == null) {
                        mUserTank = new UserTank(activity, tankColors[tankIndex]);
                        mUserDeg = mUserTank.getDegrees();
                        mJoystickColor = tankColors[tankIndex].getPaint();
                        tankIndex++;
                    } else if (!key.equals(userId) && !mOpponentTanks.containsKey(key)) {
                        mOpponentTanks.put(key,
                                new OpponentTank(activity, key, tankColors[tankIndex]));
                        addDeathDataRefListener(key);
                        tankIndex++;
                    } else {
                        tankIndex++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * Sets a listener on the game's database reference to detect an opponent leaving the game.
     * 
     * @param gameDataRef   the database reference for the game
     * @param opponentId    the id of the opponent
     */
    private void removeExitingTanks(DatabaseReference gameDataRef, final String opponentId){
        gameDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Determine if any of the children of the game has a key of the opponent id
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String key = children.getKey();

                    // Return if we have found a key matching the user id, since
                    // this means that the user has actually joined the game
                    if (key != null && key.equals(opponentId)) {
                        return;
                    }
                }

                // Remove the opponent tank when they stop playing so it is not drawn anymore
                mOpponentTanks.remove(opponentId);
                Log.d(TAG, "Removed opponent tank with id=" + opponentId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * Attach a listener on the death data reference to detect opponents dying.
     */
    private void addDeathDataRefListener(final String opponentId) {
        DatabaseReference deathDataRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USERS_KEY).child(opponentId).child(Constants.DEATH_KEY);

        deathDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long killingCannonball = dataSnapshot.getValue(Long.class);
                mUserCannonballSet.remove(killingCannonball);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * Sets the member variables of the Battle View class that are related to graphics.
     *
     * @param activity  the activity in which the battle view is created
     */
    private void setControlGraphicsData(Activity activity) {
        // Get the height and width of the device in pixels
        float screenHeight = UserUtils.getScreenHeight();
        float screenWidth = UserUtils.getScreenWidth();
        float controlHeight = screenHeight - UserUtils.scaleGraphicsInt(TOP_Y_CONST) - screenWidth;

        // Set the joystick, fire button, and control data
        mJoystickBaseRadius = Math.round(JOYSTICK_BASE_RADIUS_CONST * controlHeight);
        mJoystickThresholdRadius = Math.round(JOYSTICK_THRESHOLD_RADIUS_CONST * controlHeight);
        mJoystickMaxDisplacement = Math.round(JOYSTICK_DISPLACEMENT_CONST * mJoystickBaseRadius);
        mFireButtonDiameter = Math.round(FIRE_BUTTON_DIAMETER_CONST * controlHeight);
        mFireButtonPressedDiameter = Math.round(FIRE_BUTTON_PRESSED_DIAMETER_CONST * controlHeight);

        // Get the two bitmaps for the fire button (bigger = unpressed; smaller = pressed)
        if (mFireButtonDiameter > 0) {
            mFireBitmap = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource (activity.getResources(), R.drawable.fire_button),
                    mFireButtonDiameter, mFireButtonDiameter, false);
            mFirePressedBitmap = Bitmap.createScaledBitmap(mFireBitmap,
                    mFireButtonPressedDiameter, mFireButtonPressedDiameter, false);
        }

        // Get the margin between the joystick base and the edge of the screen
        int controlXMargin = UserUtils.scaleGraphicsInt(CONTROL_X_MARGIN_CONST);
        int controlYMargin = (int) (controlHeight - 2*mJoystickBaseRadius) / 2;

        // Set the joystick base centre, the fire button centre, and the initial joystick position
        mJoystickBaseCenterX = controlXMargin + mJoystickBaseRadius;
        mJoystickBaseCenterY = (int) screenHeight - controlYMargin - mJoystickBaseRadius;
        mJoystickX = mJoystickBaseCenterX;
        mJoystickY = mJoystickBaseCenterY;
        mFireButtonOffsetX = (int)(screenWidth - 1.5*controlXMargin - mFireButtonDiameter);
        mFireButtonOffsetY =  mJoystickBaseCenterY - mFireButtonDiameter/2;

        // Set the pointer IDs to be invalid initially
        mJoystickPointerId = MotionEvent.INVALID_POINTER_ID;
        mFireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
    }

    /**
     * Updates the location and angle of the tank based on the most recent touch events
     * by the user.
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void updateUserTank() {
        // Determine the displacement of the joystick in the x and y axes
        int deltaX = mJoystickX-mJoystickBaseCenterX;
        int deltaY = mJoystickY-mJoystickBaseCenterY;

        float velocityX = (float) (deltaX)/mJoystickThresholdRadius;
        float velocityY = (float) (deltaY)/mJoystickThresholdRadius;

        if (mUserTank == null) {
            return;
        } else if (velocityX == 0 && velocityY == 0) {
            mUserTank.update(velocityX, velocityY, mUserDeg);
        } else {
            mUserDeg = calcDegrees(deltaX, deltaY);
            mUserTank.update(velocityX, velocityY, mUserDeg);
        }
    }

    /**
     * Draws the joystick onto the canvas and draws the blue joystick controller based on how 
     * the user is moving the joystick.
     * 
     * @param canvas    the canvas on which the joystick is drawn
     */
    public void drawJoystick(Canvas canvas) {
        int controlRadius = mJoystickBaseRadius / 2;

        // Draw the base of the joystick
        Paint colors = new Paint();
        colors.setARGB(50, 50, 50, 50);
        canvas.drawCircle(mJoystickBaseCenterX, mJoystickBaseCenterY,
                mJoystickBaseRadius, colors);

        // Draw the actual joystick controller of the joystick
        canvas.drawCircle(mJoystickX, mJoystickY, controlRadius, mJoystickColor);
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
            float x = mFireButtonOffsetX + (mFireButtonDiameter-mFireButtonPressedDiameter)/2;
            float y = mJoystickBaseCenterY - mFireButtonPressedDiameter/2;
            canvas.drawBitmap(mFirePressedBitmap, (int) x, (int) y, null);
        } else {
            canvas.drawBitmap(mFireBitmap, mFireButtonOffsetX, mFireButtonOffsetY, null);
        }
    }

    /**
     * Updates the joystick's coordinate and pointer ID data using values retrieved from a touch
     * event. It changes the joystick's coordinates and pointer ID when the user moves the joystick
     * and resets this data when the user touches outside the joystick threshold boundaries.
     *
     * @param pointerX  the x coordinate of the touch event
     * @param pointerY  the y coordinate of the touch event
     * @param pointerId the pointer ID of the touch event
     * @param action    the type of action of the touch event which must be
     *                  ACTION_DOWN, ACTION_POINTER_DOWN, or ACTION_MOVE
     */
    private void updateJoystickData(int pointerX, int pointerY, int pointerId, int action) {
        // Calculate the displacement from the center of the joystick
        float deltaX = pointerX - mJoystickBaseCenterX;
        float deltaY = pointerY - mJoystickBaseCenterY;
        float displacement = calcDistance((int) deltaX, (int) deltaY);

        // Determine whether the user has touched sufficiently close to the joystick center
        if (displacement <= mJoystickMaxDisplacement) {
            mJoystickX = pointerX;
            mJoystickY = pointerY;

            // Set the pointer ID in this condition since it could be a new pointer
            mJoystickPointerId = pointerId;
        } else if ((action == MotionEvent.ACTION_MOVE && pointerId == mJoystickPointerId)
                || (displacement <= mJoystickThresholdRadius)){
            float joystickScaleRatio = (float) (mJoystickMaxDisplacement) / displacement;
            mJoystickX = (int) ((float) (mJoystickBaseCenterX) + (deltaX*joystickScaleRatio));
            mJoystickY = (int) ((float) (mJoystickBaseCenterY) + (deltaY*joystickScaleRatio));
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
        int deltaX = pointerX - (mFireButtonOffsetX + mFireButtonDiameter/2);
        int deltaY = pointerY - (mFireButtonOffsetY + mFireButtonDiameter/2);
        int displacement = calcDistance(deltaX, deltaY);

        // Determine whether the user has touched close enough to the center of the fire button
        if (displacement <= mFireButtonDiameter) {
            // Ensure only one cannonball is fired for every button press and
            // limit the number of cannonballs that can be active simultaneously
            if (!mFireButtonPressed && mUserCannonballSet.size() < MAX_USER_CANNONBALLS) {
                Cannonball cannonball = mUserTank.fire();
                mUserCannonballSet.add(cannonball);
                //Log.d(TAG, "Projectile fired at x=" + mX + " y=" + mY + " mUserDeg=" + mUserDeg + " degrees");
            }

            mFireButtonPressed = true;
            mFireButtonPointerId = pointerId;
        } else {
            mFireButtonPressed = false;
            mFireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
        }
    }

    /**
     * Removes the game from the database if necessary.
     */
    private void removeGame() {
        // Remove the game from the database if necessary
        mGameDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int numPlayers = (int) dataSnapshot.getChildrenCount() - 1;

                // Remove the game if there are no players left
                if (numPlayers == 0) {
                    mGameDataRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
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
    private int calcDegrees(int x, int y) {
        // Calculates an angle between 0 and 180 degrees
        float displacement = calcDistance(x, y);
        float angle = (float) Math.acos(x/displacement);

        // Check whether the point (x, y) is above or below the x-axis
        // to convert the angle to between 0 and 360 degrees
        if (y >= 0) {
            return (int) Math.toDegrees(angle);
        } else {
            return (int) -Math.toDegrees(angle);
        }
    }

    /**
     * Calculates the distance from the origin (0, 0) to (x, y).
     *
     * @param   x   the displacement in the x-axis
     * @param   y   the displacement in the y-axis
     * @return      the Euclidean distance calculated using the Pythagorean theorem
     */
    private int calcDistance (int x, int y) {
        return (int) Math.sqrt(x*x + y*y);
    }
}
