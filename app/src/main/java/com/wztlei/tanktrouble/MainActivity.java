package com.wztlei.tanktrouble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.wztlei.tanktrouble.database.UserUtils;

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

        UserUtils userUtils = new UserUtils(this);
    }


    private void testDatabase(){
        long startTime = System.currentTimeMillis();
        final int timeIndex = firebaseStartTimes.size();

        firebaseStartTimes.add(startTime);

        // Write a message to the database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference database = firebaseDatabase.getReference("testData/branch/sub");
        DatabaseReference database2 = firebaseDatabase.getReference();
        database2.child("users").child("thing2").child("username").setValue("dskdfjdsfkjhf");
        DatabaseReference database3 = database2.child("new").push();
        DatabaseReference databaseReference4 = firebaseDatabase.getReference("abc");
        databaseReference4.push().setValue(2345325);

        HashMap<String, Integer> coord = new HashMap<>();
        coord.put("x", 1);
        coord.put("y", 22);
        database3.setValue(coord);


        database.setValue(timeIndex);

        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = database.child("posts").push().getKey();
        UserUtils databaseUtils = new UserUtils(this);
        User user = new User(databaseUtils.generateRandomUsername());


        // Read from the database
        database.addValueEventListener(new ValueEventListener() {
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
        //testDatabase();
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
