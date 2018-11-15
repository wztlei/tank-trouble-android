package com.wztlei.tanktrouble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.wztlei.tanktrouble.battle.BattleActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WL: MainActivity.java";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        UserUtils.initialize(this);
    }

    public void onClickPlayButton(View view) {
        //Intent intent = new Intent(this, PlayActivity.class);
        //startActivity(intent);
        Intent intent = new Intent(this, BattleActivity.class);
        startActivity(intent);
        Log.d(TAG, "onClickPlayButton");

    }

    public void onClickTestButton(View view) {
        Intent intent = new Intent(this, BattleActivity.class);
        startActivity(intent);
        Log.d(TAG, "onClickTestButton");
    }

    public void onClickSettingsButton (View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}
