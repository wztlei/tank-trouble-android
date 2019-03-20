package com.wztlei.tanktrouble.battle;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.MainActivity;
import com.wztlei.tanktrouble.UserUtils;

import java.util.ArrayList;

public class BattleActivity extends AppCompatActivity {

    DatabaseReference mGameDataRef;
    String mGamePin;

    private static final String OPPONENT_IDS_KEY = Constants.OPPONENT_IDS_KEY;
    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private static final String GAMES_KEY = Constants.GAMES_KEY;
    private static final String TAG = "WL/BattleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the bundle from the previous activity
        Bundle intentBundle = getIntent().getExtras();

        // Set the content view and immediately return when the intent bundle is null
        if (intentBundle == null) {
            setContentView(new BattleView(this, new ArrayList<String>(), null));
            return;
        }

        // Get the opponent IDs and game pin string from the intent bundle
        ArrayList<String> opponentIds = intentBundle.getStringArrayList(OPPONENT_IDS_KEY);
        mGamePin = intentBundle.getString(GAME_PIN_KEY);

        Log.d(TAG, "opponentIds=" + opponentIds);

        // Immediately return if there is no game pin string
        if (mGamePin == null) {
            return;
        }

        // Display the graphics with battle view
        setContentView(new BattleView(this, opponentIds, mGamePin));

        // Grab the database reference for the game into which the user has possibly joined
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mGameDataRef = database.child(GAMES_KEY).child(mGamePin);
        validateGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        Log.d(TAG, "isFinishing()==" + isFinishing());
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");
        Log.d(TAG, "isFinishing()==" + isFinishing());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        startActivity( new Intent(this, MainActivity.class));
    }

    /**
     * Determines if the user has actually joined the game,
     * and if not, return to the main activity.
     */
    private void validateGame() {
        // Determine if the user has actually joined the game
        mGameDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Return immediately if this is a test game
                if (mGamePin.equals(Constants.TEST_GAME_PIN)) {
                    return;
                }

                String userId = UserUtils.getUserId();

                // Determine if any of the children of the game has a key of the user id
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String key = children.getKey();

                    // Return if we have found a key matching the user id, since
                    // this means that the user has actually joined the game
                    if (key != null && key.equals(userId)) {
                        return;
                    }
                }

                // The user has not actually joined the game, so return to the main activity
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
