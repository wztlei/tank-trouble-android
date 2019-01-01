package com.wztlei.tanktrouble.battle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.MainActivity;

import java.util.ArrayList;

public class BattleActivity extends AppCompatActivity {

    private static final String OPPONENT_IDS_KEY = Constants.OPPONENT_IDS_KEY;
    private static final String TAG = "WL/BattleActivity";
    private boolean backPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle intentBundle = getIntent().getExtras();
        if (intentBundle != null) {
            ArrayList<String> opponentIds = intentBundle.getStringArrayList(OPPONENT_IDS_KEY);
            if (opponentIds != null) {
                Log.d(TAG, "opponentIds=" + opponentIds + " size=" + opponentIds.size());
            } else {
                Log.d(TAG, "opponentIds=null");
            }
            setContentView(new BattleView(this, opponentIds));

        } else {
            setContentView(new BattleView(this, new ArrayList<String>()));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!backPressed) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        backPressed = true;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
