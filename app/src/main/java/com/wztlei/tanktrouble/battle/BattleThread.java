package com.wztlei.tanktrouble.battle;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class BattleThread extends Thread {

    private BattleView mBattleView;
    private final SurfaceHolder mSurfaceHolder;
    private boolean mRunning;

    /**
     * Constructor function of the BattleThread class.
     *
     * @param surfaceHolder an abstract interface to hold a display surface
     * @param battleView    a drawing surface containing the graphics for the game
     */
    BattleThread(SurfaceHolder surfaceHolder, BattleView battleView) {
        super();
        mSurfaceHolder = surfaceHolder;
        mBattleView = battleView;
    }

    /**
     * Sets the mRunning member variable to a boolean value.
     *
     * @param isRunning a boolean value storing whether BattleThread is running
     */
    public void setRunning(boolean isRunning) {
        mRunning = isRunning;
    }

    /**
     * Runs the BattleThread by repeatedly drawing and updating the canvas.
     */
    @Override
    public void run() {
        while (mRunning) {
            Canvas canvas = null;

            try {
                // Lock the canvas to prevent more than one thread from attempting to draw on it
                canvas = mSurfaceHolder.lockCanvas();

                // Draws the new canvas
                synchronized (mSurfaceHolder) {
                    if (canvas != null && mBattleView != null) {
                        mBattleView.draw(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Check whether canvas is instantiated and if so, unlock the canvas
            if (canvas != null) {
                try {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
