package com.wztlei.tanktrouble.match;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wztlei.tanktrouble.R;

public class JoinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
    }

    public void onClickEnterGamePin(View view) {
        // Get the game PIN entered by the user
        EditText editGamePin = findViewById(R.id.edit_game_pin);
        int gamePin = Integer.getInteger(editGamePin.getText().toString());

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mGameDataRef = database.child(GAMES_KEY).child(Integer.toString(mGamePin));

    }
}
