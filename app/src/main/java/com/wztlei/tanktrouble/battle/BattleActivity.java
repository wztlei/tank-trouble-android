package com.wztlei.tanktrouble.battle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class BattleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new BattleView(this));
    }
}
