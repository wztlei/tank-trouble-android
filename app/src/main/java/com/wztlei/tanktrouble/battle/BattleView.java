package com.wztlei.tanktrouble.battle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class BattleView extends SurfaceView implements SurfaceHolder.Callback {

    private BattleThread mBattleThread;
    private PlayerTank mUserTank;
    private PlayerTank mOpponentTank;
    private Context mContext;

    public BattleView(Context context) {
        super(context);

        mContext = context;

        // Callback allows us to intercept events
        getHolder().addCallback(this);

        mBattleThread = new BattleThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mUserTank = new PlayerTank(mContext, true);
        mOpponentTank = new PlayerTank(mContext, false);

        mBattleThread.setRunning(true);
        mBattleThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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
    }

    public void update() {
        mUserTank.move();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        mUserTank.draw(canvas);
        mOpponentTank.draw(canvas);
    }
}
