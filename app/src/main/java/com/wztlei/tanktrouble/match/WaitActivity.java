package com.wztlei.tanktrouble.match;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.BattleActivity;

import java.util.ArrayList;

public class WaitActivity extends AppCompatActivity {

    private String mGamePinStr;
    private String mUserId;

    private static final String TAG = "WL/WaitActivity";
    private static final String GAMES_KEY = Constants.GAMES_KEY;
    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private static final String STARTED_KEY = Constants.STARTED_KEY;
    private static final String OPPONENT_IDS_KEY = Constants.OPPONENT_IDS_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        Bundle intentBundle = getIntent().getExtras();
        mUserId = UserUtils.getUserId();

        if (intentBundle != null) {
            mGamePinStr = intentBundle.getString(GAME_PIN_KEY);
            if (mGamePinStr != null) {
                waitForGameToStart(mGamePinStr);
            }
        }
    }

    private void waitForGameToStart(String gamePinStr) {
        // Get a reference to the game that the user is waiting to start
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference waitGameDataRef = database.child(GAMES_KEY).child(gamePinStr);
        DatabaseReference startGameField = waitGameDataRef.child(STARTED_KEY);

        // Listen for new people joining the game
        startGameField.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object snapshotValue = dataSnapshot.getValue();
                Log.d(TAG, "onDataChange" + snapshotValue);

                if (snapshotValue != null && snapshotValue.toString().equals("true")) {
                    onGameStarted();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void onGameStarted() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference waitGameDataRef = database.child(GAMES_KEY).child(mGamePinStr);

        waitGameDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> opponentIDs = new ArrayList<>();

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String key = children.getKey();

                    if (key != null && !key.equals(STARTED_KEY) && !key.equals(mUserId)) {
                        opponentIDs.add(key);
                    }
                }

                Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
                intent.putExtra(OPPONENT_IDS_KEY, opponentIDs);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
