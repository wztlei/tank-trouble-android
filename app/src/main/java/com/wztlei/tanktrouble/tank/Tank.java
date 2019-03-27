package com.wztlei.tanktrouble.tank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;

import com.wztlei.tanktrouble.Constants;

public abstract class Tank {
    Bitmap mBitmap;
    int mX, mY, mDeg;
    int mWidth, mHeight;
    int mColorIndex;
    int mScore;
    boolean mIsAlive;

    static final String USERS_KEY = Constants.USERS_KEY;
    static final String POS_KEY = Constants.POS_KEY;
    static final String FIRE_KEY = Constants.FIRE_KEY;
    static final float TANK_WIDTH_CONST = Constants.TANK_WIDTH_CONST;
    static final float TANK_HEIGHT_CONST = Constants.TANK_HEIGHT_CONST;
    private static final float GUN_LENGTH_RATIO = 1/7f;
    private static final float GUN_LEFT_EDGE_RATIO = 39/100f;
    private static final float GUN_RIGHT_EDGE_RATIO = 61/100f;

    /**
     * Returns an array of PointFs representing a polygon perfectly enclosing a tank.
     *
     * @param x     the x-PointF of the tank
     * @param y     the y-PointF of the tank
     * @param deg   the angle of the tank in degrees
     * @param w     the width of the tank
     * @param h     the height of the tank
     * @return      the array of PointFs representing the polygon
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static PointF[] tankPolygon(float x, float y, float deg, float w, float h) {
        PointF rectTop, rectRight, rectBottom, rectLeft;
        PointF gunFront1, gunFront2, bodyFront1, bodyFront2, bodyFront3, bodyFront4;
        PointF bodyRear1, bodyRear2, bodyRear3, bodyRear4;
        PointF bodyLeft1, bodyLeft2, bodyLeft3, bodyLeft4, bodyLeft5, bodyLeft6, bodyLeft7;
        PointF bodyRight1, bodyRight2, bodyRight3, bodyRight4, bodyRight5, bodyRight6, bodyRight7;
        PointF gunCenter, bodyCenter;
        float risingEdge, fallingEdge, theta;

        // Get the edge lengths of the rotated bounding rectangle
        if (inRange( -180, deg,-90)) {
            risingEdge = h;
            fallingEdge = w;
            theta = (float) Math.toRadians(Math.abs(deg + 90));
        } else if (inRange( -90, deg,0)){
            risingEdge = w;
            fallingEdge = h;
            theta = (float) Math.toRadians(Math.abs(deg));
        } else if (inRange(0, deg,  90)) {
            risingEdge = h;
            fallingEdge = w;
            theta = (float) Math.toRadians(90 - deg);
        } else if (inRange( 90, deg,180)) {
            risingEdge = w;
            fallingEdge = h;
            theta = (float) Math.toRadians(180 - deg);
        } else {
            throw new IllegalArgumentException("angle must be between -180 and 180");
        }

        // Get the four corners of the rotated bounding rectangle
        rectTop = new PointF(x + risingEdge*cos(theta), y);
        rectRight = new PointF(x + risingEdge*cos(theta) + fallingEdge*sin(theta),
                y + fallingEdge*cos(theta));
        rectBottom = new PointF(x + fallingEdge*sin(theta),
                y + fallingEdge*cos(theta) + risingEdge*sin(theta));
        rectLeft = new PointF(x, y+ risingEdge*sin(theta));


        // Get the points on the left and right edge of the tank body and the front of the gun
        if (inRange( -180, deg,-90)) {
            gunFront1 = weightedMidpoint(rectLeft, rectTop, GUN_LEFT_EDGE_RATIO);
            gunFront2 = weightedMidpoint(rectLeft, rectTop, GUN_RIGHT_EDGE_RATIO);

            bodyLeft1 = weightedMidpoint(rectLeft, rectBottom, GUN_LENGTH_RATIO);
            bodyLeft2 = weightedMidpoint(rectLeft, rectBottom, 2/7f);
            bodyLeft3 = weightedMidpoint(rectLeft, rectBottom, 3/7f);
            bodyLeft4 = weightedMidpoint(rectLeft, rectBottom, 4/7f);
            bodyLeft5 = weightedMidpoint(rectLeft, rectBottom, 5/7f);
            bodyLeft6 = weightedMidpoint(rectLeft, rectBottom, 6/7f);
            bodyLeft7 = rectBottom;

            bodyRight1 = weightedMidpoint(rectTop, rectRight, GUN_LENGTH_RATIO);
            bodyRight2 = weightedMidpoint(rectTop, rectRight, 2/7f);
            bodyRight3 = weightedMidpoint(rectTop, rectRight, 3/7f);
            bodyRight4 = weightedMidpoint(rectTop, rectRight, 4/7f);
            bodyRight5 = weightedMidpoint(rectTop, rectRight, 5/7f);
            bodyRight6 = weightedMidpoint(rectTop, rectRight, 6/7f);
            bodyRight7 = rectRight;
        } else if (inRange( -90, deg,0)){
            gunFront1 = weightedMidpoint(rectTop, rectRight, GUN_LEFT_EDGE_RATIO);
            gunFront2 = weightedMidpoint(rectTop, rectRight, GUN_RIGHT_EDGE_RATIO);

            bodyLeft1 = weightedMidpoint(rectTop, rectLeft, GUN_LENGTH_RATIO);
            bodyLeft2 = weightedMidpoint(rectTop, rectLeft, 2/7f);
            bodyLeft3 = weightedMidpoint(rectTop, rectLeft, 3/7f);
            bodyLeft4 = weightedMidpoint(rectTop, rectLeft, 4/7f);
            bodyLeft5 = weightedMidpoint(rectTop, rectLeft, 5/7f);
            bodyLeft6 = weightedMidpoint(rectTop, rectLeft, 6/7f);
            bodyLeft7 = rectLeft;

            bodyRight1 = weightedMidpoint(rectRight, rectBottom, GUN_LENGTH_RATIO);
            bodyRight2 = weightedMidpoint(rectRight, rectBottom, 2/7f);
            bodyRight3 = weightedMidpoint(rectRight, rectBottom, 3/7f);
            bodyRight4 = weightedMidpoint(rectRight, rectBottom, 4/7f);
            bodyRight5 = weightedMidpoint(rectRight, rectBottom, 5/7f);
            bodyRight6 = weightedMidpoint(rectRight, rectBottom, 6/7f);
            bodyRight7 = rectBottom;
        } else if (inRange( 0, deg,90)) {
            gunFront1 = weightedMidpoint(rectRight, rectBottom, GUN_LEFT_EDGE_RATIO);
            gunFront2 = weightedMidpoint(rectRight, rectBottom, GUN_RIGHT_EDGE_RATIO);

            bodyLeft1 = weightedMidpoint(rectRight, rectTop, GUN_LENGTH_RATIO);
            bodyLeft2 = weightedMidpoint(rectRight, rectTop, 2/7f);
            bodyLeft3 = weightedMidpoint(rectRight, rectTop, 3/7f);
            bodyLeft4 = weightedMidpoint(rectRight, rectTop, 4/7f);
            bodyLeft5 = weightedMidpoint(rectRight, rectTop, 5/7f);
            bodyLeft6 = weightedMidpoint(rectRight, rectTop, 6/7f);
            bodyLeft7 = rectTop;

            bodyRight1 = weightedMidpoint(rectBottom, rectLeft, GUN_LENGTH_RATIO);
            bodyRight2 = weightedMidpoint(rectBottom, rectLeft, 2/7f);
            bodyRight3 = weightedMidpoint(rectBottom, rectLeft, 3/7f);
            bodyRight4 = weightedMidpoint(rectBottom, rectLeft, 4/7f);
            bodyRight5 = weightedMidpoint(rectBottom, rectLeft, 5/7f);
            bodyRight6 = weightedMidpoint(rectBottom, rectLeft, 6/7f);
            bodyRight7 = rectLeft;
        } else if (inRange( 90, deg, 180)) {
            gunFront1 = weightedMidpoint(rectBottom, rectLeft, GUN_LEFT_EDGE_RATIO);
            gunFront2 = weightedMidpoint(rectBottom, rectLeft, GUN_RIGHT_EDGE_RATIO);

            bodyLeft1 = weightedMidpoint(rectBottom, rectRight, GUN_LENGTH_RATIO);
            bodyLeft2 = weightedMidpoint(rectBottom, rectRight, 2/7f);
            bodyLeft3 = weightedMidpoint(rectBottom, rectRight, 3/7f);
            bodyLeft4 = weightedMidpoint(rectBottom, rectRight, 4/7f);
            bodyLeft5 = weightedMidpoint(rectBottom, rectRight, 5/7f);
            bodyLeft6 = weightedMidpoint(rectBottom, rectRight, 6/7f);
            bodyLeft7 = rectRight;

            bodyRight1 = weightedMidpoint(rectLeft, rectTop, GUN_LENGTH_RATIO);
            bodyRight2 = weightedMidpoint(rectLeft, rectTop, 2/7f);
            bodyRight3 = weightedMidpoint(rectLeft, rectTop, 3/7f);
            bodyRight4 = weightedMidpoint(rectLeft, rectTop, 4/7f);
            bodyRight5 = weightedMidpoint(rectLeft, rectTop, 5/7f);
            bodyRight6 = weightedMidpoint(rectLeft, rectTop, 6/7f);
            bodyRight7 = rectTop;
        } else {
            throw new IllegalArgumentException("angle must be between -180 and 180");
        }

        // Get the points on the front edge of the tank body
        bodyFront1 = weightedMidpoint(bodyLeft1, bodyRight1, 0.2f);
        bodyFront2 = weightedMidpoint(bodyLeft1, bodyRight1, GUN_LEFT_EDGE_RATIO);
        bodyFront3= weightedMidpoint(bodyLeft1, bodyRight1, GUN_RIGHT_EDGE_RATIO);
        bodyFront4 = weightedMidpoint(bodyLeft1, bodyRight1, 0.8f);

        // Get the points on the rear edge of the tank body
        bodyRear1 = weightedMidpoint(bodyLeft7, bodyRight7, 0.2f);
        bodyRear2 = weightedMidpoint(bodyLeft7, bodyRight7, 0.4f);
        bodyRear3 = weightedMidpoint(bodyLeft7, bodyRight7, 0.6f);
        bodyRear4 = weightedMidpoint(bodyLeft7, bodyRight7, 0.8f);

        // Get the points for the center of various parts of the gun
        gunCenter = weightedMidpoint(gunFront1, gunFront2, 0.5f);
        bodyCenter = weightedMidpoint(bodyLeft5, bodyRight5, 0.5f);

        // Return an array with all the PointFs
        return new PointF[] {gunCenter, bodyCenter, gunFront1, gunFront2,
                bodyLeft1, bodyLeft2, bodyLeft3, bodyLeft4, bodyLeft5, bodyLeft6, bodyLeft7,
                bodyRight1, bodyRight2, bodyRight3, bodyRight4, bodyRight5, bodyRight6, bodyRight7,
                bodyFront1, bodyFront2, bodyFront3, bodyFront4,
                bodyRear1, bodyRear2, bodyRear3, bodyRear4};
    }

    /**
     * Returns an array of PointFs representing a hitbox perfectly enclosing the tank body.
     * The hitbox determines if a cannonball has hit the tank.
     *
     * @param x     the x-PointF of the tank
     * @param y     the y-PointF of the tank
     * @param deg   the angle of the tank in degrees
     * @param w     the width of the tank
     * @param h     the height of the tank
     * @return      the array of PointFs representing the polygon
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static PointF[] tankHitbox(float x, float y, float deg, float w, float h) {
        PointF rectTop, rectRight, rectBottom, rectLeft;
        PointF bodyFront1, bodyFront2, bodyFront3;
        PointF midA1, midA2, midA3;
        PointF midB1, midB2, midB3;
        PointF midC1, midC2, midC3;
        PointF midD1, midD2, midD3;
        PointF midE1, midE2, midE3;
        PointF bodyRear1, bodyRear2, bodyRear3;
        PointF bodyLeft1, bodyLeft2, bodyLeft3, bodyLeft4, bodyLeft5, bodyLeft6, bodyLeft7;
        PointF bodyRight1, bodyRight2, bodyRight3, bodyRight4, bodyRight5, bodyRight6, bodyRight7;
        float risingEdge, fallingEdge, theta;

        // Get the edge lengths of the rotated bounding rectangle
        if (inRange( -180, deg,-90)) {
            risingEdge = h;
            fallingEdge = w;
            theta = (float) Math.toRadians(Math.abs(deg + 90));
        } else if (inRange( -90, deg,0)){
            risingEdge = w;
            fallingEdge = h;
            theta = (float) Math.toRadians(Math.abs(deg));
        } else if (inRange(0, deg,  90)) {
            risingEdge = h;
            fallingEdge = w;
            theta = (float) Math.toRadians(90 - deg);
        } else if (inRange( 90, deg,180)) {
            risingEdge = w;
            fallingEdge = h;
            theta = (float) Math.toRadians(180 - deg);
        } else {
            throw new IllegalArgumentException("angle must be between -180 and 180");
        }

        // Get the four corners of the rotated bounding rectangle
        rectTop = new PointF(x + risingEdge*cos(theta), y);
        rectRight = new PointF(x + risingEdge*cos(theta) + fallingEdge*sin(theta),
                y + fallingEdge*cos(theta));
        rectBottom = new PointF(x + fallingEdge*sin(theta),
                y + fallingEdge*cos(theta) + risingEdge*sin(theta));
        rectLeft = new PointF(x, y+ risingEdge*sin(theta));

        // Get the points on the left and right edge of the tank body and the front of the gun
        if (inRange( -180, deg,-90)) {
            bodyLeft1 = weightedMidpoint(rectLeft, rectBottom, GUN_LENGTH_RATIO);
            bodyLeft2 = weightedMidpoint(rectLeft, rectBottom, 2/7f);
            bodyLeft3 = weightedMidpoint(rectLeft, rectBottom, 3/7f);
            bodyLeft4 = weightedMidpoint(rectLeft, rectBottom, 4/7f);
            bodyLeft5 = weightedMidpoint(rectLeft, rectBottom, 5/7f);
            bodyLeft6 = weightedMidpoint(rectLeft, rectBottom, 6/7f);
            bodyLeft7 = rectBottom;

            bodyRight1 = weightedMidpoint(rectTop, rectRight, GUN_LENGTH_RATIO);
            bodyRight2 = weightedMidpoint(rectTop, rectRight, 2/7f);
            bodyRight3 = weightedMidpoint(rectTop, rectRight, 3/7f);
            bodyRight4 = weightedMidpoint(rectTop, rectRight, 4/7f);
            bodyRight5 = weightedMidpoint(rectTop, rectRight, 5/7f);
            bodyRight6 = weightedMidpoint(rectTop, rectRight, 6/7f);
            bodyRight7 = rectRight;
        } else if (inRange( -90, deg,0)){
            bodyLeft1 = weightedMidpoint(rectTop, rectLeft, GUN_LENGTH_RATIO);
            bodyLeft2 = weightedMidpoint(rectTop, rectLeft, 2/7f);
            bodyLeft3 = weightedMidpoint(rectTop, rectLeft, 3/7f);
            bodyLeft4 = weightedMidpoint(rectTop, rectLeft, 4/7f);
            bodyLeft5 = weightedMidpoint(rectTop, rectLeft, 5/7f);
            bodyLeft6 = weightedMidpoint(rectTop, rectLeft, 6/7f);
            bodyLeft7 = rectLeft;

            bodyRight1 = weightedMidpoint(rectRight, rectBottom, GUN_LENGTH_RATIO);
            bodyRight2 = weightedMidpoint(rectRight, rectBottom, 2/7f);
            bodyRight3 = weightedMidpoint(rectRight, rectBottom, 3/7f);
            bodyRight4 = weightedMidpoint(rectRight, rectBottom, 4/7f);
            bodyRight5 = weightedMidpoint(rectRight, rectBottom, 5/7f);
            bodyRight6 = weightedMidpoint(rectRight, rectBottom, 6/7f);
            bodyRight7 = rectBottom;
        } else if (inRange( 0, deg,90)) {
            bodyLeft1 = weightedMidpoint(rectRight, rectTop, GUN_LENGTH_RATIO);
            bodyLeft2 = weightedMidpoint(rectRight, rectTop, 2/7f);
            bodyLeft3 = weightedMidpoint(rectRight, rectTop, 3/7f);
            bodyLeft4 = weightedMidpoint(rectRight, rectTop, 4/7f);
            bodyLeft5 = weightedMidpoint(rectRight, rectTop, 5/7f);
            bodyLeft6 = weightedMidpoint(rectRight, rectTop, 6/7f);
            bodyLeft7 = rectTop;

            bodyRight1 = weightedMidpoint(rectBottom, rectLeft, GUN_LENGTH_RATIO);
            bodyRight2 = weightedMidpoint(rectBottom, rectLeft, 2/7f);
            bodyRight3 = weightedMidpoint(rectBottom, rectLeft, 3/7f);
            bodyRight4 = weightedMidpoint(rectBottom, rectLeft, 4/7f);
            bodyRight5 = weightedMidpoint(rectBottom, rectLeft, 5/7f);
            bodyRight6 = weightedMidpoint(rectBottom, rectLeft, 6/7f);
            bodyRight7 = rectLeft;
        } else if (inRange( 90, deg, 180)) {
            bodyLeft1 = weightedMidpoint(rectBottom, rectRight, GUN_LENGTH_RATIO);
            bodyLeft2 = weightedMidpoint(rectBottom, rectRight, 2/7f);
            bodyLeft3 = weightedMidpoint(rectBottom, rectRight, 3/7f);
            bodyLeft4 = weightedMidpoint(rectBottom, rectRight, 4/7f);
            bodyLeft5 = weightedMidpoint(rectBottom, rectRight, 5/7f);
            bodyLeft6 = weightedMidpoint(rectBottom, rectRight, 6/7f);
            bodyLeft7 = rectRight;

            bodyRight1 = weightedMidpoint(rectLeft, rectTop, GUN_LENGTH_RATIO);
            bodyRight2 = weightedMidpoint(rectLeft, rectTop, 2/7f);
            bodyRight3 = weightedMidpoint(rectLeft, rectTop, 3/7f);
            bodyRight4 = weightedMidpoint(rectLeft, rectTop, 4/7f);
            bodyRight5 = weightedMidpoint(rectLeft, rectTop, 5/7f);
            bodyRight6 = weightedMidpoint(rectLeft, rectTop, 6/7f);
            bodyRight7 = rectTop;
        } else {
            throw new IllegalArgumentException("angle must be between -180 and 180");
        }

        // Get the points on the front edge of the tank body
        bodyFront1 = weightedMidpoint(bodyLeft1, bodyRight1, 0.25f);
        bodyFront2 = weightedMidpoint(bodyLeft1, bodyRight1, 0.50f);
        bodyFront3= weightedMidpoint(bodyLeft1, bodyRight1, 0.75f);

        // Get the points for the first midsection
        midA1 = weightedMidpoint(bodyLeft2, bodyRight2, 0.25f);
        midA2 = weightedMidpoint(bodyLeft2, bodyRight2, 0.50f);
        midA3= weightedMidpoint(bodyLeft2, bodyRight2, 0.75f);

        // Get the points for the second midsection
        midB1 = weightedMidpoint(bodyLeft3, bodyRight3, 0.25f);
        midB2 = weightedMidpoint(bodyLeft3, bodyRight3, 0.50f);
        midB3= weightedMidpoint(bodyLeft3, bodyRight3, 0.75f);

        // Get the points for the third midsection
        midC1 = weightedMidpoint(bodyLeft4, bodyRight4, 0.25f);
        midC2 = weightedMidpoint(bodyLeft4, bodyRight4, 0.50f);
        midC3= weightedMidpoint(bodyLeft4, bodyRight4, 0.75f);

        // Get the points for the fourth midsection
        midD1 = weightedMidpoint(bodyLeft5, bodyRight5, 0.25f);
        midD2 = weightedMidpoint(bodyLeft5, bodyRight5, 0.50f);
        midD3= weightedMidpoint(bodyLeft5, bodyRight5, 0.75f);

        // Get the points for the fifth midsection
        midE1 = weightedMidpoint(bodyLeft6, bodyRight6, 0.25f);
        midE2 = weightedMidpoint(bodyLeft6, bodyRight6, 0.50f);
        midE3= weightedMidpoint(bodyLeft6, bodyRight6, 0.75f);

        // Get the points on the rear edge of the tank body
        bodyRear1 = weightedMidpoint(bodyLeft7, bodyRight7, 0.25f);
        bodyRear2 = weightedMidpoint(bodyLeft7, bodyRight7, 0.50f);
        bodyRear3 = weightedMidpoint(bodyLeft7, bodyRight7, 0.75f);

        // Return an array with all the PointFs
        return new PointF[] {midA1, midA2, midA3, midB1, midB2, midB3,
                midC1, midC2, midC3, midD1, midD2, midD3, midE1, midE2, midE3,
                bodyLeft1, bodyLeft2, bodyLeft3, bodyLeft4, bodyLeft5, bodyLeft6, bodyLeft7,
                bodyRight1, bodyRight2, bodyRight3, bodyRight4, bodyRight5, bodyRight6, bodyRight7,
                bodyFront1, bodyFront2, bodyFront3, bodyRear1, bodyRear2, bodyRear3};
    }

    /**
     * Returns the weighted midpoint M between two points pt1 and pt2.
     *
     * @param   pt1     the smaller value
     * @param   pt2     the bigger value
     * @param   weight  the ratio of the distance AM to distance AB
     * @return          the weighted midpoint
     * @throws          IllegalArgumentException when weight is outside the interval [0, 1]
     */
    private static PointF weightedMidpoint(PointF pt1, PointF pt2, float weight) {
        // Check that that the weight is within the interval [0, 1]
        if (!inRange(0, weight, 1)) {
            throw new IllegalArgumentException("weight is not between 0 and 1 inclusive");
        } else {
            return new PointF(pt1.x + (pt2.x - pt1.x) * weight,
                    pt1.y + (pt2.y - pt1.y) * weight);
        }
    }

    /**
     * Draws the tank bitmap onto a canvas with the proper rotation.
     *
     * @param canvas the canvas on which the tank is drawn.
     */
    public void draw(Canvas canvas) {
        if (mX != 0 || mY != 0) {
            Matrix matrix = new Matrix();
            matrix.setRotate(mDeg);
            Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                    mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
            canvas.drawBitmap(rotatedBitmap, mX, mY, null);
        }
    }

    public void kill() {

    }

    public PointF getCenter() {
        return Tank.tankHitbox(mX, mY, mDeg, mWidth, mHeight)[7];
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getDegrees() {
        return mDeg;
    }

    public int getColorIndex() {
        return mColorIndex;
    }

    public int getScore() {
        return mScore;
    }

    public void incrementScore() {
        mScore++;
        // TODO: Update the value in Firebase and read that score
    }

    public boolean isAlive() {
        return mIsAlive;
    }

    /**
     * Returns true if num is in the interval [min, max] and false otherwise.
     *
     * @param min   the minimum value of num
     * @param num   the number to test
     * @param max   the maximum value of num
     * @return      true if num is in the interval [min, max] and false otherwise
     */
    private static boolean inRange (float min, float num, float max) {
        return (min <= num && num <= max);
    }

    private static float cos(float rad) {
        return (float) Math.cos(rad);
    }

    private static float sin(float rad) {
        return (float) Math.sin(rad);
    }
}
