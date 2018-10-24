package com.wztlei.tanktrouble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import com.wztlei.tanktrouble.battle.BattleActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideFirestoreLogWarning();

        FirestoreUserUtils mFirestoreUserUtils = new FirestoreUserUtils(this);
        mFirestoreUserUtils.setUsername();
    }

    public void onClickPlayButton(View view) {
        Intent intent = new Intent(this, PlayActivity.class);
        startActivity(intent);
    }

    public void onClickPlayTestButton(View view) {
        Intent intent = new Intent(this, BattleActivity.class);
        startActivity(intent);
    }

    public void onClickSettingsButton (View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

/*
    public void testFirestore (View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);

        final String TAG = "TAG";

        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(  new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }*/

    private void hideFirestoreLogWarning () {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
    }
}
