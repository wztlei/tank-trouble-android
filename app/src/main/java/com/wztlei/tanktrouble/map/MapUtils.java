package com.wztlei.tanktrouble.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.tank.Tank;

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
            return null;
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

        if (DEFAULT_MAP_WALLS == null) {
            return;
        }

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
        PointF[] tankPolygon = Tank.tankPolygon(x, y, deg, w, h);

        if (DEFAULT_MAP_WALLS == null) {
            return false;
        }

        // Go through all the walls of the map
        for (RectF wall : DEFAULT_MAP_WALLS) {
            // Check if the tank could possibly cross a wall using the larger bounding rectangle
            if (RectF.intersects(boundingRect, wall)) {
                // There is a possibility the tank could cross a wall,
                // so we need to iterate through all the points of the tank polygon
                for (PointF pointF : tankPolygon) {
                    // We know a tank position is immediately invalid if any point is in the wall
                    if (wall.contains(pointF.x, pointF.y)) {
                        return true;
                    }
                }
            }
        }

        // The tank position passed all the filters, so there is no collision
        return false;
    }

    public static boolean cannonballWallCollision(float x, float y, int r) {
        return cannonballWallCollision((int) x, (int) y, r);
    }

    /**
     * Returns true if the cannonball collides with a wall and false otherwise.
     * and false otherwise.
     *
     * @param x     the x-coordinate of the cannonball
     * @param y     the y-coordinate of the cannonball
     * @param r     the radius of the cannonball
     * @return      true if the cannonball is in a valid position, and false otherwise
     */
    @SuppressWarnings({"SuspiciousNameCombination", "SimplifiableIfStatement"})
    private static boolean cannonballWallCollision(int x, int y, int r) {
        // Store the location of the cannonball relative to the map cell
        int cellRow = (y - TOP_Y) / CELL_WIDTH;
        int cellCol =  x / CELL_WIDTH;
        int cellX = x % CELL_WIDTH;
        int cellY = (y - TOP_Y) % CELL_WIDTH;

        // Perform a check for an intersection with a right or bottom map boundary wall
        if (cellRow >= NUM_CELL_ROWS || cellCol >= NUM_CELL_COLS) {
            return true;
        }

        // Store a reference to the map cell of the cannonball
        MapCell mapCell = DEFAULT_MAP_CELLS[cellRow][cellCol];

        // Perform a check for an intersection with an edge of the map cell or a corner
        if (inRange(WALL_WIDTH+r, cellX, CELL_WIDTH-r)
                && inRange(WALL_WIDTH+r, cellY, CELL_WIDTH-r)) {
            // Return false for a position that is always wall-free (ie. in the centre of a cell)
            return false;
        } else if (mapCell.hasLeftWall() && cellX < WALL_WIDTH+r) {
            // Return true for an intersection with a left wall
            return true;
        } else if (mapCell.hasTopWall() && cellY < WALL_WIDTH+r) {
            // Return true for an intersection with a top wall
            return true;
        } else if (mapCell.hasRightWall() && cellX > CELL_WIDTH-r) {
            // Return true for an intersection with a right wall
            return true;
        } else if (mapCell.hasBottomWall() && cellY > CELL_WIDTH-r) {
            // Return true for an intersection with a bottom wall
            return true;
        } else {
            // The cannonball does not collide with an edge so check for a collision with a corner
            return cannonballCornerCollision(cellRow, cellCol, cellX, cellY, r);
        }
    }

    /**
     * Returns true if the cannonball collides with a wall corner and false otherwise.
     *
     * @param cellRow   the cell row within the grid of map cells
     * @param cellCol   the cell col within the grid of map cells
     * @param cellX     the x-coordinate within the cell
     * @param cellY     the y-coordinate within the cell
     * @param r         the radius of the cannonball
     * @return          true if the cannonball collides with a wall corner and false otherwise
     */
    @SuppressWarnings({"SuspiciousNameCombination", "RedundantIfStatement"})
    private static boolean cannonballCornerCollision
            (int cellRow, int cellCol, int cellX, int cellY, int r) {
        // Declare variables to store the existence of intersections with corners
        Rect boundingRect = new Rect(cellX-r, cellY-r, cellX+r, cellY+r);

        // Determine if a top left corner can even exist
        if ((cellRow > 0) && (cellCol > 0)) {
            MapCell topLeftCell = DEFAULT_MAP_CELLS[cellRow-1][cellCol-1];
            boolean tlCorner = topLeftCell.hasRightWall() || topLeftCell.hasBottomWall();

            // Detect a collision with a top left corner
            if (tlCorner && (calcDistance(cellX, cellY, WALL_WIDTH, WALL_WIDTH) < r
                    || boundingRect.intersect(new Rect(0, 0, WALL_WIDTH, WALL_WIDTH)))) {
                return true;
            }
        }

        // Determine if a top right corner can even exist
        if ((cellRow > 0) && (cellCol < NUM_CELL_COLS-1)) {
            MapCell topRightCell = DEFAULT_MAP_CELLS[cellRow-1][cellCol+1];
            boolean trCorner = topRightCell.hasBottomWall() || topRightCell.hasLeftWall();

            // Detect a collision with a top right corner
            if (trCorner && (calcDistance(cellX, cellY, CELL_WIDTH, WALL_WIDTH) < r
                    || boundingRect.intersect(new Rect(CELL_WIDTH, 0,
                    CELL_WIDTH+WALL_WIDTH, WALL_WIDTH)))) {
                return true;
            }
        }

        // Determine if a bottom right corner can even exist
        if ((cellRow < NUM_CELL_ROWS-1) && (cellCol < NUM_CELL_COLS-1)) {
            MapCell bottomRightCell = DEFAULT_MAP_CELLS[cellRow+1][cellCol+1];
            boolean brCorner = bottomRightCell.hasLeftWall() || bottomRightCell.hasTopWall();

            // Detect a collision with a bottom right corner
            if (brCorner && (calcDistance(cellX, cellY, CELL_WIDTH, CELL_WIDTH) < r
                    || boundingRect.intersect(new Rect(CELL_WIDTH, CELL_WIDTH,
                    CELL_WIDTH+WALL_WIDTH, CELL_WIDTH+WALL_WIDTH)))) {
                return true;
            }
        }

        // Determine if a bottom left corner can even exist
        if ((cellRow < NUM_CELL_ROWS-1) && (cellCol > 0)) {
            MapCell bottomLeftCell = DEFAULT_MAP_CELLS[cellRow+1][cellCol-1];
            boolean blCorner = bottomLeftCell.hasTopWall() || bottomLeftCell.hasRightWall();

            // Detect a collision with a bottom left corner
            if (blCorner && (calcDistance(cellX, cellY, WALL_WIDTH, CELL_WIDTH) < r
                    || boundingRect.intersect(new Rect(0, CELL_WIDTH, WALL_WIDTH,
                    CELL_WIDTH+WALL_WIDTH)))) {
                return true;
            }
        }

        // The cannonball has passed all the tests, so it does not with a wall corner
        return false;
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
}
