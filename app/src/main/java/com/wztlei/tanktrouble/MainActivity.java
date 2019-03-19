package com.wztlei.tanktrouble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.wztlei.tanktrouble.battle.BattleActivity;
import com.wztlei.tanktrouble.match.HostActivity;
import com.wztlei.tanktrouble.match.JoinActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WL/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        UserUtils.initialize(this);
    }

    public void onClickHostButton(View view) {
        Intent intent = new Intent(this, HostActivity.class);
        startActivity(intent);
        Log.d(TAG, "onClickHostButton");
    }

    public void onClickJoinButton(View view) {
        Intent intent = new Intent(this, JoinActivity.class);
        startActivity(intent);
        Log.d(TAG, "onClickJoinButton");
    }

    public void onClickTestButton(View view) {
        Intent intent = new Intent(this, BattleActivity.class);
        intent.putExtra(Constants.GAME_PIN_KEY, Constants.TEST_GAME_PIN);
        startActivity(intent);
        Log.d(TAG, "onClickTestButton");
    }

    public void onClickSettingsButton (View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        Log.d(TAG, "onClickSettingsButton");

    }
}
