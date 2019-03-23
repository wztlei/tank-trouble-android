package com.wztlei.tanktrouble.cannonball;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wztlei.tanktrouble.tank.UserTank;

import java.util.ArrayList;

import static com.wztlei.tanktrouble.Constants.DEATH_KEY;
import static com.wztlei.tanktrouble.Constants.FIRE_KEY;
import static com.wztlei.tanktrouble.Constants.USERS_KEY;

public class CannonballSet {

    private SparseArray<Cannonball> mCannonballSet;

    private static final long CANNONBALL_LIFESPAN = 10000;
    private static final String TAG = "WL/CannonballSet";

    /**
     * Initializes the set of cannonballs as represented by a concurrent hash map.
     */
    public CannonballSet() {
        mCannonballSet = new SparseArray<>();
    }

    /**
     * Attach a listener on the death data reference to detect opponents dying.
     */
    public void addOpponent(final String opponentId) {
        DatabaseReference usersDataRef =
                FirebaseDatabase.getInstance().getReference().child(USERS_KEY);
        addFireDataRefListener(usersDataRef, opponentId);
        addDeathDataRefListener(usersDataRef, opponentId);
    }

    /**
     * Attach a listener on the fire data reference to detect opponents firing cannonballs.
     */
    private void addFireDataRefListener(DatabaseReference usersDataRef, String opponentId) {
        usersDataRef.child(opponentId).child(FIRE_KEY)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Path path = dataSnapshot.getValue(Path.class);

                        if (path != null && path.getCoordinates() != null) {
                            ArrayList<Coordinate> pathCoordinates = path.getCoordinates();

                            for (Coordinate coordinate : pathCoordinates) {
                                coordinate.scale();
                            }

                            mCannonballSet.put(path.getUUID(), new Cannonball(pathCoordinates));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    /**
     * Add a listener for an opponent dying to remove the cannonball that hit the opponent.
     *
     * @param userDataRef   the data reference of all the users
     * @param opponentId    the user id of the opponent
     */
    private void addDeathDataRefListener(DatabaseReference userDataRef, String opponentId) {
        userDataRef.child(opponentId).child(DEATH_KEY)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Integer killingCannonball = dataSnapshot.getValue(Integer.class);

                        if (killingCannonball != null) {
                            mCannonballSet.remove(killingCannonball);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    /**
     * Adds a new cannonball to the set.
     *
     * @param cannonball the cannonball to be added
     */
    public void addCannonball(Cannonball cannonball) {
        mCannonballSet.put(cannonball.getUUID(), cannonball);
    }

    /**
     * Adds a new cannonball to the set.
     *
     * @param cannonball the cannonball to be added
     */
    public void add(int uuid, Cannonball cannonball) {
        Log.d(TAG, "add" +uuid);
        mCannonballSet.put(uuid, cannonball);
    }

    /**
     * Removes a cannonball from the set.
     *
     * @param uuid  the uuid as a string of the cannonball to be removed
     */
    public void remove(int uuid) {
        mCannonballSet.remove(uuid);
    }

    /**
     * Returns the number of cannonballs in the set.
     *
     * @return the number of cannonballs in the set.
     */
    public int size(){
        return mCannonballSet.size();
    }

    @SuppressWarnings("unused")
    public boolean containsKey(int key) {
        return mCannonballSet.get(key) != null;
    }

    /**
     * Updates the location and direction of all the cannonballs,
     * removes cannonballs after they have exceeded their lifespan,
     * and detects if a collision has occurred with the user.
     *
     * @return the uuid string key if a cannonball collided with the user, and null otherwise
     */
    public int updateAndDetectUserCollision(UserTank userTank) {
        int detectedUserCollision = 0;
        long nowTime = System.currentTimeMillis();
        ArrayList<Integer> keysToRemove =  new ArrayList<>();

        // Iterate through all the cannonballs in the set
        for(int i = 0; i < mCannonballSet.size(); i++) {
            int key = mCannonballSet.keyAt(i);
            // get the object by the key.
            Cannonball cannonball = mCannonballSet.get(key);
            long deltaTime = nowTime - cannonball.getFiringTime();

            // Check whether the cannonball has exceeded its lifespan and remove if necessary
            if (deltaTime > CANNONBALL_LIFESPAN) {
                keysToRemove.add(key);
            } else {
                cannonball.update();

                if (userTank.detectCollision(cannonball)) {
                    detectedUserCollision = key;
                    keysToRemove.add(key);
                }
            }
        }

        for (int i = 0; i < keysToRemove.size(); i++) {
            mCannonballSet.remove(keysToRemove.get(i));
        }

        return detectedUserCollision;
    }

    /**
     * Draws all the cannonballs onto a canvas.
     *
     * @param canvas the canvas onto which the cannonballs are drawn
     */
    public void draw(Canvas canvas) {
        // Iterate through all the cannonballs in the set and draw them
        for (int i = 0; i < mCannonballSet.size(); i++) {
            mCannonballSet.valueAt(i).draw(canvas);
        }
    }
}
