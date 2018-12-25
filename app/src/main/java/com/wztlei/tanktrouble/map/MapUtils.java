package com.wztlei.tanktrouble.map;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.wztlei.tanktrouble.Globals;
import com.wztlei.tanktrouble.UserUtils;

public class MapUtils {

    //private static final String LTRB = "LTRB";
    //private static final String TRB = "TRB";
    //private static final String LTR = "LTR";
    private static final String TR = "TR";
    //private static final String LTB = "LTB";
    private static final String TB = "TB";
    private static final String LT = "LT";
    private static final String T = "T";
    private static final String LRB = "LRB";
    private static final String RB = "RB";
    private static final String LR = "LR";
    private static final String R = "R";
    private static final String LB = "LB";
    private static final String B = "B";
    private static final String L = "L";
    private static final String O = "";
    //private static final String TAG = "WL/MapUtils";
    
    private static final float GUN_LENGTH_RATIO = 1/7f;
    private static final float GUN_LEFT_EDGE_RATIO = 39/100f;
    private static final float GUN_RIGHT_EDGE_RATIO = 61/100f;
    private static final float TOP_Y = UserUtils.scaleGraphics(Globals.MAP_TOP_Y_SCALE);
    private static final float CELL_WIDTH =
            UserUtils.scaleGraphics(Globals.MAP_CELL_WIDTH_SCALE);
    private static final float WALL_WIDTH =
            UserUtils.scaleGraphics(Globals.MAP_WALL_WIDTH_SCALE);

    private static final MapCell[][] DEFAULT_MAP = new MapCell[][] {
            {new MapCell(LT), new MapCell(T), new MapCell(T), new MapCell(T), new MapCell(TR)},
            {new MapCell(L), new MapCell(R), new MapCell(LRB), new MapCell(LRB), new MapCell(LR)},
            {new MapCell(LR), new MapCell(L), new MapCell(T), new MapCell(T), new MapCell(R)},
            {new MapCell(LR), new MapCell(LB), new MapCell(O), new MapCell(R), new MapCell(LR)},
            {new MapCell(LB), new MapCell(TB), new MapCell(B), new MapCell(RB), new MapCell(LRB)}};

    //         _ _ _ _ _
    //        |         |
    //        |   |_|_| |
    //        | |       |
    //        | |_ _  | |
    //        |_ _ _ _|_|

    /**
     * Draws the map onto the canvas.
     *
     * @param canvas    the canvas on which the map is drawn
     */
    public static void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setARGB(255, 234, 234, 234);
        canvas.drawRect(0, TOP_Y, UserUtils.getScreenWidth(),
                TOP_Y+UserUtils.getScreenWidth(), paint);

        paint.setARGB(255, 120, 120, 120);

        // Iterate through all the cells in the map
        for (int row = 0; row < DEFAULT_MAP.length; row++) {
            for (int col = 0; col < DEFAULT_MAP[row].length; col++) {
                MapCell mapCell = DEFAULT_MAP[row][col];

                // Draw the left wall if needed
                if (mapCell.hasLeftWall()) {
                    canvas.drawRect(CELL_WIDTH*col, TOP_Y + CELL_WIDTH*row,
                            CELL_WIDTH*col + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH + WALL_WIDTH, paint);
                }
                
                // Draw the top wall if needed
                if (mapCell.hasTopWall()) {
                    canvas.drawRect(CELL_WIDTH*col, TOP_Y + CELL_WIDTH*row, 
                            CELL_WIDTH*col + CELL_WIDTH + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + WALL_WIDTH, paint);
                }

                // Draw the right wall if needed
                if (mapCell.hasRightWall()) {
                    canvas.drawRect(CELL_WIDTH*col + CELL_WIDTH, TOP_Y + CELL_WIDTH*row,
                            CELL_WIDTH*col + +CELL_WIDTH + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH + WALL_WIDTH, paint);
                }

                // Draw the bottom wall if needed
                if (mapCell.hasBottomWall()) {
                    canvas.drawRect(CELL_WIDTH*col, TOP_Y + CELL_WIDTH*row + CELL_WIDTH,
                            CELL_WIDTH*col + CELL_WIDTH + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH + WALL_WIDTH, paint);
                }
            }
        }
    }

    /**
     * Returns an array of coordinates representing a polygon perfectly enclosing a tank.
     * The array has eight elements which correspond to the eight points of the polygon in
     * clockwise order.
     *
     * @param x     the x-coordinate of the tank
     * @param y     the y-coordinate of the tank
     * @param deg   the angle of the tank in degrees
     * @param w     the width of the tank
     * @param h     the height of the tank
     * @return      the array of coordinates representing the polygon
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static Coordinate[] tankPolygon(float x, float y, float deg, float w, float h) {
        Coordinate top, right, bottom, left;
        Coordinate tankRearLeft, tankRearRight, tankFrontLeft, tankFrontRight;
        Coordinate gunRearLeft, gunRearRight, gunFrontLeft, gunFrontRight;
        float risingEdge, fallingEdge, theta;

        // Get the edge lengths of the rotated bounding rectangle
        if (isBetween(deg, -180, -90)) {
            risingEdge = h;
            fallingEdge = w;
            theta = (float) Math.toRadians(Math.abs(deg + 90));
        } else if (isBetween(deg, -90, 0)){
            risingEdge = w;
            fallingEdge = h;
            theta = (float) Math.toRadians(Math.abs(deg));
        } else if (isBetween(deg, 0, 90)) {
            risingEdge = h;
            fallingEdge = w;
            theta = (float) Math.toRadians(90 - deg);
        } else if (isBetween(deg, 90, 180)) {
            risingEdge = w;
            fallingEdge = h;
            theta = (float) Math.toRadians(180 - deg);
        } else {
            throw new IllegalArgumentException("angle must be between -180 and 180");
        }

        // Get the four corners of the rotated bounding rectangle
        top = new Coordinate(x + risingEdge*cos(theta), y);
        right = new Coordinate(x + risingEdge*cos(theta) + fallingEdge*sin(theta),
                y + fallingEdge*cos(theta));
        bottom = new Coordinate(x + fallingEdge*sin(theta),
                y + fallingEdge*cos(theta) + risingEdge*sin(theta));
        left = new Coordinate(x, y+ risingEdge*sin(theta));

        // Get the coordinates of the tank and the front of the gun
        if (isBetween(deg, -180, -90)) {
            tankRearLeft = bottom;
            tankRearRight = right;
            tankFrontLeft = weightedMidpoint(left, bottom, GUN_LENGTH_RATIO);
            tankFrontRight = weightedMidpoint(top, right, GUN_LENGTH_RATIO);
            gunFrontLeft = weightedMidpoint(left, top, GUN_LEFT_EDGE_RATIO);
            gunFrontRight = weightedMidpoint(left, top, GUN_RIGHT_EDGE_RATIO);
        } else if (isBetween(deg, -90, 0)){
            tankRearLeft = left;
            tankRearRight = bottom;
            tankFrontLeft = weightedMidpoint(top, left, GUN_LENGTH_RATIO);
            tankFrontRight = weightedMidpoint(right, bottom, GUN_LENGTH_RATIO);
            gunFrontLeft = weightedMidpoint(top, right, GUN_LEFT_EDGE_RATIO);
            gunFrontRight = weightedMidpoint(top, right, GUN_RIGHT_EDGE_RATIO);
        } else if (isBetween(deg, 0, 90)) {
            tankRearLeft = top;
            tankRearRight = left;
            tankFrontLeft = weightedMidpoint(right, top, GUN_LENGTH_RATIO);
            tankFrontRight = weightedMidpoint(bottom, left, GUN_LENGTH_RATIO);
            gunFrontLeft = weightedMidpoint(right, bottom, GUN_LEFT_EDGE_RATIO);
            gunFrontRight = weightedMidpoint(right, bottom, GUN_RIGHT_EDGE_RATIO);
        } else if (isBetween(deg, 90, 180)) {
            tankRearLeft = right;
            tankRearRight = top;
            tankFrontLeft = weightedMidpoint(bottom, right, GUN_LENGTH_RATIO);
            tankFrontRight = weightedMidpoint(left, top, GUN_LENGTH_RATIO);
            gunFrontLeft = weightedMidpoint(bottom, left, GUN_LEFT_EDGE_RATIO);
            gunFrontRight = weightedMidpoint(bottom, left, GUN_RIGHT_EDGE_RATIO);
        } else {
            throw new IllegalArgumentException("angle must be between -180 and 180");
        }

        // Get the rear coordinates of the gun
        gunRearLeft = weightedMidpoint(tankFrontLeft, tankFrontRight, GUN_LEFT_EDGE_RATIO);
        gunRearRight= weightedMidpoint(tankFrontLeft, tankFrontRight, GUN_RIGHT_EDGE_RATIO);

        // Return an array with all the coordinates in clockwise order
        return new Coordinate[] {tankFrontLeft, gunRearLeft, gunFrontLeft, gunFrontRight,
                gunRearRight, tankFrontRight, tankRearRight, tankRearLeft};
    }

    /**
     * Returns the weighted midpoint M between two coordinates pt1 and pt2.
     *
     * @param   pt1     the smaller value
     * @param   pt2     the bigger value
     * @param   weight  the ratio of the distance AM to distance AB
     * @return          the weighted midpoint
     * @throws          IllegalArgumentException when weight is outside the interval [0, 1]
     */
    private static Coordinate weightedMidpoint(Coordinate pt1, Coordinate pt2, float weight) {

        if (!isBetween(weight, 0, 1)) {
            throw new IllegalArgumentException("weight is not between 0 and 1 inclusive");
        } else {
            return new Coordinate(pt1.x + (pt2.x - pt1.x) * weight,
                    pt1.y + (pt2.y - pt1.y) * weight);
        }
    }

    private static boolean isBetween (float x, float min, float max) {
        return (min <= x && x <= max);
    }

    private static float cos(float rad) {
        return (float) Math.cos(rad);
    }

    private static float sin(float rad) {
        return (float) Math.sin(rad);
    }
}
