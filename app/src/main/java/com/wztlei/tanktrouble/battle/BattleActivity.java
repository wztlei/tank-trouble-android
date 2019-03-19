package com.wztlei.tanktrouble.battle;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
    String mGamePinStr;

    private static final String OPPONENT_IDS_KEY = Constants.OPPONENT_IDS_KEY;
    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private static final String GAMES_KEY = Constants.GAMES_KEY;

    private static final String TAG = "WL/BattleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Document this class
        Bundle intentBundle = getIntent().getExtras();

        if (intentBundle == null) {
            setContentView(new BattleView(this, new ArrayList<String>()));
            return;
        }

        ArrayList<String> opponentIds = intentBundle.getStringArrayList(OPPONENT_IDS_KEY);
        mGamePinStr = intentBundle.getString(GAME_PIN_KEY);

        if (mGamePinStr == null) {
            return;
        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mGameDataRef = database.child(GAMES_KEY).child(mGamePinStr);

        mGameDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mGamePinStr.equals(Constants.TEST_GAME_PIN)) {
                    return;
                }

                String userId = UserUtils.getUserId();

                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String key = children.getKey();

                    if (key != null && key.equals(userId)) {
                        return;
                    }
                }

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        setContentView(new BattleView(this, opponentIds));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGameDataRef != null) {
            mGameDataRef.child(UserUtils.getUserId()).removeValue();

            mGameDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int numPlayers = (int) dataSnapshot.getChildrenCount() - 1;

                    if (numPlayers == 0) {
                        mGameDataRef.removeValue();
                        Log.d(TAG, "Game with PIN=" + mGamePinStr + " has been deleted.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
