package com.wztlei.tanktrouble.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;

import java.util.ArrayList;

public class MapUtils {

//    private static final String LTRB = "LTRB";
//    private static final String TRB = "TRB";
//    private static final String LTR = "LTR";
    private static final String TR = "TR";
    private static final String LTB = "LTB";
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
//    private static final String O = "";
    private static final String TAG = "WL/MapUtils";

    private static final float GUN_LENGTH_RATIO = 1/7f;
    private static final float GUN_LEFT_EDGE_RATIO = 39/100f;
    private static final float GUN_RIGHT_EDGE_RATIO = 61/100f;
    private static final int TOP_Y =
            UserUtils.scaleGraphicsInt(Constants.MAP_TOP_Y_CONST);
    private static final int CELL_WIDTH =
            UserUtils.scaleGraphicsInt(Constants.MAP_CELL_WIDTH_CONST);
    private static final int WALL_WIDTH =
            UserUtils.scaleGraphicsInt(Constants.MAP_WALL_WIDTH_CONST);

    private static final MapCell[][] DEFAULT_MAP_CELLS = new MapCell[][] {
            {new MapCell(LTB), new MapCell(T), new MapCell(T), new MapCell(T), new MapCell(TR)},
            {new MapCell(LT), new MapCell(R), new MapCell(LRB), new MapCell(LRB), new MapCell(LR)},
            {new MapCell(LR), new MapCell(L), new MapCell(T), new MapCell(T), new MapCell(R)},
            {new MapCell(LR), new MapCell(LB), new MapCell(R), new MapCell(LR), new MapCell(LR)},
            {new MapCell(LB), new MapCell(TB), new MapCell(B), new MapCell(RB), new MapCell(LRB)}};

    private static final ArrayList<RectF> DEFAULT_MAP_WALLS = cellsToWalls(DEFAULT_MAP_CELLS);
    private static final int NUM_CELL_ROWS = DEFAULT_MAP_CELLS.length;
    private static final int NUM_CELL_COLS = DEFAULT_MAP_CELLS[0].length;

    //         _ _ _ _ _
    //        |_        |
    //        |   |_|_| |
    //        | |       |
    //        | |_  | | |
    //        |_ _ _ _|_|

    /**
     * Converts a grid of MapCells to an array list of rectangle walls.
     *
     * @param   cellGrid    a grid of MapCells
     * @return              an ArrayList of rectangle walls
     */
    private static ArrayList<RectF> cellsToWalls(MapCell[][] cellGrid) {
        ArrayList<RectF> mapWalls = new ArrayList<>();

        if (TOP_Y == 0 || WALL_WIDTH == 0 || CELL_WIDTH == 0) {
            throw new IllegalStateException("one of TOP_Y, WALL_WIDTH, or CELL_WIDTH is zero");
        }

        // Iterate through all the rows and columns of a cell grid
        for (int row = 0; row < cellGrid.length; row++) {
            for (int col = 0; col < cellGrid[row].length; col++) {
                MapCell mapCell = cellGrid[row][col];

                // Add the left wall if needed
                if (mapCell.hasLeftWall() && col == 0) {
                    mapWalls.add(new RectF(CELL_WIDTH*col,
                            TOP_Y + CELL_WIDTH*row,
                            CELL_WIDTH*col + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH + WALL_WIDTH));
                }

                // Add the top wall if needed
                if (mapCell.hasTopWall() && row == 0) {
                    mapWalls.add(new RectF(CELL_WIDTH*col,
                            TOP_Y + CELL_WIDTH*row,
                            CELL_WIDTH*col + CELL_WIDTH + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + WALL_WIDTH));
                }

                // Add the right wall if needed
                if (mapCell.hasRightWall()) {
                    mapWalls.add(new RectF(CELL_WIDTH*col + CELL_WIDTH,
                            TOP_Y + CELL_WIDTH*row,
                            CELL_WIDTH*col + +CELL_WIDTH + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH + WALL_WIDTH));
                }

                // Add the bottom wall if needed
                if (mapCell.hasBottomWall()) {
                    mapWalls.add(new RectF(CELL_WIDTH*col,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH,
                            CELL_WIDTH*col + CELL_WIDTH + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH + WALL_WIDTH));
                }
            }
        }

        return mapWalls;
    }

    /**
     * Draws the map onto the canvas.
     *
     * @param canvas    the canvas on which the map is drawn
     */
    public static void drawMap(Canvas canvas) {
        Paint paint = new Paint();
        paint.setARGB(255, 230, 230, 230);
        canvas.drawRect(0, TOP_Y, UserUtils.getScreenWidth(),
                TOP_Y+UserUtils.getScreenWidth(), paint);

        paint.setARGB(255, 120, 120, 120);

        // Iterate through all the cells in the map
        for (RectF wall : DEFAULT_MAP_WALLS) {
            canvas.drawRect(wall, paint);
        }
    }

    /**
     * Returns true if the tank is in a valid position and false otherwise.
     *
     * @param x     the x-coordinate of the tank
     * @param y     the y-coordinate of the tank
     * @param deg   the angle of the tank in degrees
     * @param w     the width of the tank
     * @param h     the height of the tank
     * @return      true if the tank is in a valid position and false otherwise
     */
    public static boolean tankWallCollision(float x, float y, float deg, float w, float h) {
        RectF boundingRect = new RectF(x, y, x+w+h, y+w+h);
        PointF[] tankPolygon = tankPolygon(x, y, deg, w, h);

        // Go through all the walls of the map
        for (RectF wall : DEFAULT_MAP_WALLS) {
            // Check if the tank could possibly cross a wall using the larger bounding rectangle
            if (RectF.intersects(boundingRect, wall)) {
                // There is a possibility the tank could cross a wall,
                // so we need to iterate through all the points of the tank polygon
                for (PointF pointF : tankPolygon) {
                    // We know a tank position is immediately invalid if any point is in the wall
                    if (wall.contains(pointF.x, pointF.y)) {
                        return false;
                    }
                }
            }
        }

        // The tank position passed all the filters, so the position is valid
        return true;
    }

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
     * The hitbox determines if a projectile has hit the tank.
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
     * Returns true if the cannonball is in a valid position and not colliding with a wall,
     * and false otherwise.
     *
     * @param x     the x-coordinate of the cannonball
     * @param y     the y-coordinate of the cannonball
     * @param r     the radius of the cannonball
     * @return      true if the cannonball is in a valid position, and false otherwise
     */
    @SuppressWarnings({"SuspiciousNameCombination", "RedundantIfStatement"})
    public static boolean cannonballWallCollision(int x, int y, int r) {
        // Store the location of the cannonball relative to the map cell
        int cellRow = (y - TOP_Y) / CELL_WIDTH;
        int cellCol =  x / CELL_WIDTH;
        int cellX = x % CELL_WIDTH;
        int cellY = (y - TOP_Y) % CELL_WIDTH;

        // Perform a check for an intersection with a right or bottom map boundary wall
        if (cellRow >= NUM_CELL_ROWS || cellCol >= NUM_CELL_COLS) {
            return false;
        }

        // Store a reference to the map cell of the cannonball
        MapCell mapCell = DEFAULT_MAP_CELLS[cellRow][cellCol];

        // Perform a check for an intersection with an edge of the map cell
        if (inRange(WALL_WIDTH+r, cellX, CELL_WIDTH-r)
                && inRange(WALL_WIDTH+r, cellY, CELL_WIDTH-r)) {
            // Return true for a position that is always wall-free (ie. in the centre of a cell)
            return true;
        } else if (mapCell.hasLeftWall() && cellX < WALL_WIDTH+r) {
            // Return false for an intersection with a left wall
            return false;
        } else if (mapCell.hasTopWall() && cellY < WALL_WIDTH+r) {
            // Return false for an intersection with a top wall
            return false;
        } else if (mapCell.hasRightWall() && cellX > CELL_WIDTH-r) {
            // Return false for an intersection with a right wall
            return false;
        } else if (mapCell.hasBottomWall() && cellY > CELL_WIDTH-r) {
            // Return false for an intersection with a bottom wall
            return false;
        }

        // Declare variables to store the existence of intersections with corners
        Rect boundingRect = new Rect(cellX-r, cellY-r, cellX+r, cellY+r);
        boolean bottomLeftCorner = (cellRow < NUM_CELL_ROWS-1) && (cellCol > 0);

        // Detect a collision with a top left corner
        if ((cellRow > 0) && (cellCol > 0)) {
            MapCell topLeftCell = DEFAULT_MAP_CELLS[cellRow-1][cellCol-1];
            boolean tlCorner = topLeftCell.hasRightWall() || topLeftCell.hasBottomWall();

            if (tlCorner && (calcDistance(cellX, cellY, WALL_WIDTH, WALL_WIDTH) < r
                    || boundingRect.intersect(new Rect(0, 0, WALL_WIDTH, WALL_WIDTH)))) {
                return false;
            }
        }

        // Detect a collision with a top right corner
        if ((cellRow > 0) && (cellCol < NUM_CELL_COLS-1)) {
            MapCell topRightCell = DEFAULT_MAP_CELLS[cellRow-1][cellCol+1];
            boolean trCorner = topRightCell.hasBottomWall() || topRightCell.hasLeftWall();

            if (trCorner && (calcDistance(cellX, cellY, CELL_WIDTH, WALL_WIDTH) < r
                    || boundingRect.intersect(new Rect(CELL_WIDTH, 0,
                    CELL_WIDTH+WALL_WIDTH, WALL_WIDTH)))) {
                return false;
            }
        }

        // Detect a collision with a bottom right corner
        if ((cellRow < NUM_CELL_ROWS-1) && (cellCol < NUM_CELL_COLS-1)) {
            MapCell bottomRightCell = DEFAULT_MAP_CELLS[cellRow+1][cellCol+1];
            boolean brCorner = bottomRightCell.hasLeftWall() || bottomRightCell.hasTopWall();

            if (brCorner && (calcDistance(cellX, cellY, CELL_WIDTH, CELL_WIDTH) < r
                    || boundingRect.intersect(new Rect(CELL_WIDTH, CELL_WIDTH,
                    CELL_WIDTH+WALL_WIDTH, CELL_WIDTH+WALL_WIDTH)))) {
                return false;
            }
        }

        // Detect a collision with a bottom left corner
        if (bottomLeftCorner) {
            MapCell bottomLeftCell = DEFAULT_MAP_CELLS[cellRow+1][cellCol-1];
            bottomLeftCorner = bottomLeftCell.hasTopWall() || bottomLeftCell.hasRightWall();

            if (bottomLeftCorner && (calcDistance(cellX, cellY, WALL_WIDTH, CELL_WIDTH) < r
                    || boundingRect.intersect(new Rect(0, CELL_WIDTH, WALL_WIDTH,
                    CELL_WIDTH+WALL_WIDTH)))) {
                return false;
            }
        }

        // No collision with wall
        return true;
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
        if (!inRange( 0, weight,1)) {
            throw new IllegalArgumentException("weight is not between 0 and 1 inclusive");
        } else {
            return new PointF(pt1.x + (pt2.x - pt1.x) * weight,
                    pt1.y + (pt2.y - pt1.y) * weight);
        }
    }

    /**
     * Calculates the Euclidean distance between two points, given a displacement in the x-axis
     * and a displacement in the y-axis using the Pythagorean theorem.
     *
     * @param x1    the x-coordinate of the first point
     * @param y1    the y-coordinate of the first point
     * @param x2    the x-coordinate of the second point
     * @param y2    the y-coordinate of the second point
     * @return      the distance between the two points
     */
    private static float calcDistance (float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
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
