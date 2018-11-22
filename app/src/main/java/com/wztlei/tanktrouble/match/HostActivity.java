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

    private TextView mTextPlayersReady;
    private DatabaseReference mGameDataRef;
    private String mGamePinStr;

    private static final String TAG = "WL: HostActivity";
    private static final String GAMES_KEY = Globals.GAMES_KEY;
    private static final String USER_ID_KEY = Globals.USER_ID_KEY;
    private static final int MIN_GAME_PIN = 1000;
    private static final int MAX_GAME_PIN = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // Display and set the game PIN
        TextView mTextGamePin = findViewById(R.id.text_game_pin);
        mTextPlayersReady = findViewById(R.id.text_players_ready);
        mTextGamePin.setText(mGamePinStr);

        //
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(mGamePinStr)) {
                    mGamePinStr = Integer.toString(randomInt(MIN_GAME_PIN, MAX_GAME_PIN));
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Get the user id
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = sharedPref.getString(USER_ID_KEY, "");

        if (userId.length() > 0) {
            ArrayList<String> players = new ArrayList<>();
            players.add(userId);


            mGameDataRef = database.child(GAMES_KEY).child(Integer.toString(mGamePin));
            mGameDataRef.setValue(players);

            mGameDataRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int numPlayers = (int) dataSnapshot.getChildrenCount();

                    if (numPlayers == 1) {
                        String newPlayersReadyText = "1 Player Ready";
                        mTextPlayersReady.setText(newPlayersReadyText);

                    } else {
                        String newPlayersReadyText = numPlayers + " Players Ready";
                        mTextPlayersReady.setText(newPlayersReadyText);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            Log.e(TAG, "Warning: no user Id");
        }
    }

    private void setRandomGamePin() {

    }

    private int randomInt (int min, int max){
        Random random = new Random();
        return random.nextInt(max-min+1) + min;

    }

    public void onClickStartGame(View view) {
        mGamePinStr = Integer.toString(randomInt(MIN_GAME_PIN, MAX_GAME_PIN));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(mGamePinStr)) {
                    mGamePinStr = Integer.toString(randomInt(MIN_GAME_PIN, MAX_GAME_PIN));
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
