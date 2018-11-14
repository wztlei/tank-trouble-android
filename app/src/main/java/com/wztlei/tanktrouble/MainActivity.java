package com.wztlei.tanktrouble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import com.wztlei.tanktrouble.battle.BattleActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private static final String TAG = "WL: MainActivity.java";
    ArrayList<Long> firebaseStartTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /*mFirestore = FirebaseFirestore.getInstance();

        hideFirestoreLogWarning();
        testFirestore();
        //FirebaseFirestore.setLoggingEnabled(true);
        FirestoreUserUtils mFirestoreUserUtils = new FirestoreUserUtils(this);
        mFirestoreUserUtils.setUsername();*/
    }

    /*private void testFirestore() {
        long startTime = System.currentTimeMillis();
        firebaseStartTimes.add(startTime);


        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "New");
        user.put("last", "Lovelace");
        user.put("born", 1815);

        final int timeIndex = firebaseStartTimes.size();
        Log.d(TAG, "testFirestore timeIndex=" + timeIndex);


// Add a new document with a generated ID
        mFirestore.collection("testUsers" )
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        long endTime = System.currentTimeMillis();
                        long responseTime = endTime - firebaseStartTimes.get(timeIndex);
                        Log.d(TAG, "firebase response time for item num=" + timeIndex + " is time=" + responseTime);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }*/

    private void testDatabase(){
        long startTime = System.currentTimeMillis();
        final int timeIndex = firebaseStartTimes.size();

        firebaseStartTimes.add(startTime);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("testData/branch/sub");
        myRef.setValue(timeIndex);



        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue().toString();
                Log.d(TAG, "Value is: " + value);

                long endTime = System.currentTimeMillis();
                long responseTime = endTime - firebaseStartTimes.get(timeIndex);
                Log.d(TAG, "firebase response time for item num=" + timeIndex + " is time=" + responseTime);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    public void onClickPlayButton(View view) {
        //Intent intent = new Intent(this, PlayActivity.class);
        //startActivity(intent);
        Intent intent = new Intent(this, BattleActivity.class);
        startActivity(intent);

    }

    public void onClickTestButton(View view) {
        testDatabase();
    }

    public void onClickSettingsButton (View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void hideFirestoreLogWarning () {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);
    }
}
