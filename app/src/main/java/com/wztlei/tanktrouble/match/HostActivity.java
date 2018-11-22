package com.wztlei.tanktrouble.match;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wztlei.tanktrouble.Globals;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.battle.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HostActivity extends AppCompatActivity {

    private TextView mTextGamePin;
    private TextView mTextPlayersReady;
    private DatabaseReference mGamesDataRef;
    private int mGamePin;
    private ArrayList<String> testPlayers = new ArrayList<>();


    private static final String TAG = "WL: HostActivity";
    private static final String GAMES_KEY = Globals.GAMES_KEY;
    private static final int MIN_GAME_PIN = 1000;
    private static final int MAX_GAME_PIN = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // Display and set the game PIN
        mTextGamePin = findViewById(R.id.text_game_pin);
        mTextPlayersReady = findViewById(R.id.text_players_ready);
        mGamePin = randomInt(MIN_GAME_PIN, MAX_GAME_PIN);
        String gamePinStr = Integer.toString(mGamePin);
        mTextGamePin.setText(gamePinStr);

        // Get the user id
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = sharedPref.getString(GAMES_KEY, "");

        if (userId.length() > 0) {
            ArrayList<String> players = new ArrayList<>();
            players.add(userId);

            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            mGamesDataRef = database.child(GAMES_KEY);
            mGamesDataRef.child(Integer.toString(mGamePin))
                    .setValue(players);

            mGamesDataRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList players = dataSnapshot.getValue(ArrayList.class);

                    if (players == null) {
                        Log.d(TAG, "Null players");
                    } else {
                        int numPlayers = players.size();
                        String newPlayersReadyText = numPlayers + "Players Ready";
                        mTextPlayersReady.setText(newPlayersReadyText);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            // TODO: Listen for people joining the game

        } else {
            Log.e(TAG, "Warning: no user Id");
        }


    }

    private int randomInt (int min, int max){
        Random random = new Random();
        return random.nextInt(max-min+1) + min;

    }

    public void onClickStartGame(View view) {
        testPlayers.add("asd");

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mGamesDataRef = database.child(GAMES_KEY);
        mGamesDataRef.child(Integer.toString(mGamePin))
                .setValue(testPlayers);
    }
}
