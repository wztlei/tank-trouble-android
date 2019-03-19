package com.wztlei.tanktrouble.projectile;

import android.graphics.Canvas;
import android.util.Log;

import com.wztlei.tanktrouble.battle.UserTank;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CannonballSet {

    private ConcurrentHashMap<UUID, Cannonball> mCannonballSet;

    private static final long CANNONBALL_LIFESPAN = 10000;
    private static final String TAG = "WL/CannonballSet";

    /**
     * Initializes the set of cannonballs as represented by a concurrent hash map.
     */
    public CannonballSet() {
        mCannonballSet = new ConcurrentHashMap<>();
    }

    /**
     * Adds a new cannonball to the set.
     *
     * @param cannonball the cannonball to be added
     */
    public void add(Cannonball cannonball) {
        UUID uuid = UUID.randomUUID();
        mCannonballSet.put(uuid, cannonball);
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
     * @return true if a cannonball collided with the user, and false otherwise
     */
    public boolean updateAndDetectUserCollision(UserTank userTank) {
        long nowTime = System.currentTimeMillis();
        boolean detectedUserCollision = false;

        // Iterate through all the cannonballs in the set
        for (HashMap.Entry<UUID, Cannonball> entry : mCannonballSet.entrySet()) {
            UUID key = entry.getKey();
            Cannonball cannonball = entry.getValue();
            long deltaTime = nowTime - cannonball.getFiringTime();

            // Check whether the cannonball has exceeded its lifespan and remove if necessary
            if (deltaTime > CANNONBALL_LIFESPAN) {
                mCannonballSet.remove(key);
            } else {
                cannonball.update();

                Log.d(TAG, "x=" + cannonball.getX() + " y=" + cannonball.getY());

                if (userTank.detectCollision(cannonball)) {
                    // TODO: Uncomment to detect user collision
//                    detectedUserCollision = true;
//                    mCannonballSet.remove(key);
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
