package com.wztlei.tanktrouble.match;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;

public class JoinActivity extends AppCompatActivity {

    DatabaseReference mGamesDataRef;
    String mUserId;
    String mGamePinStr;
    boolean mWaitActivityStarting;

    private static final String GAMES_KEY = Constants.GAMES_KEY;
    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private static final String TAG = "WL/JoinActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // Get a reference to the games database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mGamesDataRef = database.child(GAMES_KEY);

        mUserId = UserUtils.getUserId();
        mWaitActivityStarting = false;


        // Log an error if there is no user ID
        if (mUserId != null && mUserId.length() == 0) {
            Log.e(TAG, "No user ID set");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWaitActivityStarting = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove the user from the game list if the user has not entered the waiting lobby
        if (!mWaitActivityStarting && mGamePinStr != null) {
            mGamesDataRef.child(mGamePinStr).child(mUserId).removeValue();
        }
    }

    /**
     * Called whenever the user clicks the button to enter a game PIN.
     * The method attempts to allow the user to join that game.
     *
     * @param view the button that is clicked
     */
    public void onClickEnterGamePin(View view) {
        // Get the game PIN entered by the user
        EditText editGamePin = findViewById(R.id.edit_game_pin);
        mGamePinStr = editGamePin.getText().toString();

        mGamesDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mGamePinStr.length() > 0 && dataSnapshot.hasChild(mGamePinStr)) {
                    mWaitActivityStarting = true;
                    joinGame();
                } else {
                    createInvalidPinDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * Allows the user to join the game by adding the user's id to that game's database.
     * The method also starts the wait activity which functions as a waiting lobby.
     */
    private void joinGame() {
        mGamesDataRef.child(mGamePinStr)
                .child(mUserId)
                .setValue("guest")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(getApplicationContext(), WaitActivity.class);
                        intent.putExtra(GAME_PIN_KEY, mGamePinStr);
                        startActivity(intent);
                    }
                });
    }

    /**
     *  Display an alert dialog with a single button saying Ok.
     *
     */
    private void createInvalidPinDialog() {

        // Build an alert dialog using the title and message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setMessage("We didn't recognize that game PIN. \nPlease try again.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        // Get the AlertDialog from create() and show it
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
