package com.wztlei.tanktrouble.match;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.wztlei.tanktrouble.R;

import java.util.Random;

public class HostActivity extends AppCompatActivity {

    private TextView mTextGamePin;
    private TextView mTextPlayersReady;
    private int mGamePin;

    private static final String TAG = "WL: HostActivity";
    private static final int MIN_GAME_PIN = 1000;
    private static final int MAX_GAME_PIN = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        mTextGamePin = findViewById(R.id.text_game_pin);
        mTextPlayersReady = findViewById(R.id.text_players_ready);
        mGamePin = randomInt(MIN_GAME_PIN, MAX_GAME_PIN);

        mTextGamePin.setText(mGamePin);
        // TODO: Listen for people joining the game
    }

    private int randomInt (int min, int max){
        Random random = new Random();
        return random.nextInt(max-min+1) + min;

    }
}
