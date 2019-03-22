package com.wztlei.tanktrouble.projectile;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.wztlei.tanktrouble.tank.UserTank;

import java.util.HashSet;
import java.util.Iterator;

public class CannonballSet {

    private HashSet<Cannonball> mCannonballSet;

    private static final long CANNONBALL_LIFESPAN = 10000;
    private static final String TAG = "WL/CannonballSet";

    /**
     * Initializes the set of cannonballs as represented by a concurrent hash map.
     */
    public CannonballSet() {
        mCannonballSet = new HashSet<>();
    }

    /**
     * Adds a new cannonball to the set.
     *
     * @param cannonball the cannonball to be added
     */
    public void add(Cannonball cannonball) {
        mCannonballSet.add(cannonball);
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
    public PointF updateAndDetectUserCollision(UserTank userTank) {
        PointF detectedUserCollision = null;
        long nowTime = System.currentTimeMillis();

        // Iterate through all the cannonballs in the set
        for (Iterator<Cannonball> iterator = mCannonballSet.iterator(); iterator.hasNext();) {
            Cannonball cannonball = iterator.next();
            long deltaTime = nowTime - cannonball.getFiringTime();

            // Check whether the cannonball has exceeded its lifespan and remove if necessary
            if (deltaTime > CANNONBALL_LIFESPAN) {
                iterator.remove();
            } else {
                cannonball.update();

                if (userTank.detectCollision(cannonball)) {
                    detectedUserCollision = userTank.getCenter();
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
        for (Cannonball cannonball : mCannonballSet) {
            cannonball.draw(canvas);
        }
    }
}
