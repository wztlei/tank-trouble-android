package com.wztlei.tanktrouble.battle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wztlei.tanktrouble.R;

public class BattleActivity extends AppCompatActivity {

    private String mOpponentUserId;
    private String mOpponentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new BattleView(this));
    }
}
