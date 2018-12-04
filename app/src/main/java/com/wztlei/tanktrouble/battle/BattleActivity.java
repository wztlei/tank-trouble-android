package com.wztlei.tanktrouble.battle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wztlei.tanktrouble.Globals;

import java.util.ArrayList;

public class BattleActivity extends AppCompatActivity {

    private static final String OPPONENT_IDS_KEY = Globals.OPPONENT_IDS_KEY;
    private static final String TAG = "WL/BattleActivity";

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
}
