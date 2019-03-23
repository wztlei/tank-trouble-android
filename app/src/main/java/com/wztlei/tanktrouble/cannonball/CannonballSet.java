package com.wztlei.tanktrouble.cannonball;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;

import com.wztlei.tanktrouble.tank.UserTank;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CannonballSet {

    private HashMap<Long, Cannonball> mCannonballSet;

    private static final long CANNONBALL_LIFESPAN = 10000;
    private static final String TAG = "WL/CannonballSet";

    /**
     * Initializes the set of cannonballs as represented by a concurrent hash map.
     */
    @SuppressLint("UseSparseArrays")
    public CannonballSet() {
        mCannonballSet = new HashMap<>();
    }

    /**
     * Adds a new cannonball to the set.
     *
     * @param cannonball the cannonball to be added
     */
    public void add(Cannonball cannonball) {
        mCannonballSet.put(cannonball.getUUID(), cannonball);
    }

    /**
     * Adds a new cannonball to the set.
     *
     * @param cannonball the cannonball to be added
     */
    public void add(Long uuid, Cannonball cannonball) {
        Log.d(TAG, "" +uuid);
        mCannonballSet.put(uuid, cannonball);
    }

    /**
     * Removes a cannonball from the set.
     *
     * @param uuid  the uuid as a string of the cannonball to be removed
     */
    public void remove(Long uuid) {
        if (!mCannonballSet.containsKey(uuid)) {
            Log.d(TAG, "" +uuid);
        }
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

    /**
     * Updates the location and direction of all the cannonballs,
     * removes cannonballs after they have exceeded their lifespan,
     * and detects if a collision has occurred with the user.
     *
     * @return the uuid string key if a cannonball collided with the user, and null otherwise
     */
    public Long updateAndDetectUserCollision(UserTank userTank) {
        long detectedUserCollision = 0;
        long nowTime = System.currentTimeMillis();

        // Iterate through all the cannonballs in the set
        for (Iterator<Map.Entry<Long, Cannonball>> iterator =
             mCannonballSet.entrySet().iterator(); iterator.hasNext(); ) {

            Map.Entry<Long, Cannonball> pair = iterator.next();
            Cannonball cannonball = pair.getValue();
            long deltaTime = nowTime - cannonball.getFiringTime();

            // Check whether the cannonball has exceeded its lifespan and remove if necessary
            if (deltaTime > CANNONBALL_LIFESPAN) {
                iterator.remove();
            } else {
                cannonball.update();

                if (userTank.detectCollision(cannonball)) {
                    detectedUserCollision = pair.getKey();
                    iterator.remove();
                }
            }
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
        for (Cannonball cannonball : mCannonballSet.values()) {
            cannonball.draw(canvas);
        }
    }
}
