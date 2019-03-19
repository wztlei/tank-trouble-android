package com.wztlei.tanktrouble.match;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.MainActivity;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.BattleActivity;

import java.util.ArrayList;
import java.util.Random;

public class HostActivity extends AppCompatActivity {

    private DatabaseReference mGamesDataRef;
    private String mGamePinStr;
    private String mUserId;
    private boolean mBattleActivityStarting;

    private static final String TAG = "WL/HostActivity";
    private static final String GAMES_KEY = Constants.GAMES_KEY;
    private static final String STARTED_KEY = Constants.STARTED_KEY;
    private static final String OPPONENT_IDS_KEY = Constants.OPPONENT_IDS_KEY;
    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private static final int MIN_GAME_PIN = 1000;
    private static final int MAX_GAME_PIN = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // Get a reference to the games database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mGamesDataRef = database.child(GAMES_KEY);

        // Get the user id
        mUserId = UserUtils.getUserId();

        // Set the random game pin
        hostGameWithRandomPin();

        mBattleActivityStarting = false;
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        onUniqueRandomPinCreated();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!mBattleActivityStarting) {
            mGamesDataRef.child(mGamePinStr).removeValue();
        }
    }

    /**
     * Creates a new game in firebase that is hosted by the current user.
     */
    private void hostGameWithRandomPin() {
        // Create a new random game pin and display it
        mGamePinStr = Integer.toString(UserUtils.randomInt(MIN_GAME_PIN, MAX_GAME_PIN));
        TextView textGamePin = findViewById(R.id.text_game_pin);
        String textGamePinStr = "PIN: " + mGamePinStr;
        textGamePin.setText(textGamePinStr);

        // Grab data in the games database
        mGamesDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if there already exists a game with the newly created random pin
                if (dataSnapshot.hasChild(mGamePinStr)) {
                    // If so, retry hosting with game with a new random pin
                    hostGameWithRandomPin();
                } else if (mUserId != null && mUserId.length() > 0) {
                    // Process the game pin once it has been created
                    onUniqueRandomPinCreated();

                    // Automatically display that one player is ready (which is the current user)
                    TextView textPlayersReady = findViewById(R.id.text_players_ready);
                    String newPlayersReadyText = "1 Player Ready";
                    textPlayersReady.setText(newPlayersReadyText);
                } else {
                    Log.e(TAG, "Warning: no user Id");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * Displays the game pin on the screen and sets a listener to update a text view displaying
     * the number of people that are waiting to play the game.
     */
    private void onUniqueRandomPinCreated() {
        if (mUserId == null) {
            startActivity(new Intent(this, MainActivity.class));
        }

        // Get a reference to the game that is being hosted by the current user
        DatabaseReference hostGameDataRef = mGamesDataRef.child(mGamePinStr);
        hostGameDataRef.child(mUserId).setValue("host");
        hostGameDataRef.child(STARTED_KEY).setValue("false");

        // Listen for new people joining the game
        hostGameDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView textPlayersReady = findViewById(R.id.text_players_ready);
                int numPlayers = (int) dataSnapshot.getChildrenCount() - 1;

                // Update the text displaying how many people have joined the game
                if (numPlayers == 1) {
                    String newPlayersReadyText = "1 Player Ready";
                    textPlayersReady.setText(newPlayersReadyText);
                } else {
                    String newPlayersReadyText = numPlayers + " Players Ready";
                    textPlayersReady.setText(newPlayersReadyText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * Starts the multiplayer game by starting the battle activity.
     *
     * @param view the button that is clicked
     */
    public void onClickStartGame(View view) {
        DatabaseReference hostGameDataRef = mGamesDataRef.child(mGamePinStr);
        hostGameDataRef.child(STARTED_KEY).setValue("true");

        hostGameDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> opponentIDs = new ArrayList<>();

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String key = children.getKey();

                    if (key != null && !key.equals(STARTED_KEY) && !key.equals(mUserId)) {
                        opponentIDs.add(key);
                    }
                }

                mBattleActivityStarting = true;
                Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
                intent.putExtra(OPPONENT_IDS_KEY, opponentIDs);
                intent.putExtra(GAME_PIN_KEY, mGamePinStr);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
