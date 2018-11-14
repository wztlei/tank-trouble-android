package com.wztlei.tanktrouble.database;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wztlei.tanktrouble.Globals;
import com.wztlei.tanktrouble.R;

import java.util.Random;

public class DatabaseUtils {
    private DatabaseReference mDatabase;
    private Activity mActivity;
    private SharedPreferences mSharedPref;
    private EditText mEditUsername;
    private String[] mAdjectiveList;
    private String[] mNounList;
    private String mUsername;
    private String mNewUsername;


    private static final String USERS_KEY = "users";
    private static final String USERNAME_KEY = Globals.USERNAME_KEY;
    private static final String TAG = "WL: DatabaseUtils";

    DatabaseUtils(Activity activity){

        mActivity = activity;
        mAdjectiveList = activity.getResources().getStringArray(R.array.adjective_list);
        mNounList = activity.getResources().getStringArray(R.array.noun_list);
        mEditUsername = activity.findViewById(R.id.edit_username);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        mUsername = mSharedPref.getString(USERNAME_KEY, "");
        Log.d(TAG, "mUsername=" + mUsername);

        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    private void updateUsername(final String newUsername){
        // Generates a new random username if the current username is empty
        if (!newUsername.equals(mUsername)) {
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(newUsername)) {
                        createOkAlertDialog("Username taken. Please choose another username.");
                    } else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, databaseError.getMessage());
                }
            });
        }

        Log.d(TAG, "mUsername=" + mUsername);


    }

    private void addFirebaseUser() {
    }

    private void updateUserName() {
    }

    /**
     * Concatenates two random adjectives from mAdjectiveList and one random noun
     * from mNounList to produce a random username.
     *
     * @return  a string storing the randomly generated username
     */
    private String generateRandomUsername() {

        Random random = new Random();

        // Get two adjectives and one noun
        String adjective1 = mAdjectiveList[random.nextInt(mAdjectiveList.length)];
        String adjective2 = mAdjectiveList[random.nextInt(mAdjectiveList.length)];
        String noun = mNounList[random.nextInt(mNounList.length)];

        // Capitalize each word with the rest of the string being lowercase characters
        adjective1 = adjective1.substring(0, 1).toUpperCase()
                + adjective1.substring(1).toLowerCase();
        adjective2 = adjective2.substring(0, 1).toUpperCase()
                + adjective2.substring(1).toLowerCase();
        noun = noun.substring(0, 1).toUpperCase()
                + noun.substring(1).toLowerCase();

        return adjective1 + adjective2 + noun;
    }

    /**
     * Puts a string in the SharedPreferences object which is a key-value pair system.
     *
     * @param key   the key used to determine where the string is to be stored
     * @param value the actual string being stored in SharedPreferences
     */
    private void putStringInPrefs (String key, String value) {
        SharedPreferences.Editor mSharedPrefEditor = mSharedPref.edit();
        mSharedPrefEditor.putString(key, value);
        mSharedPrefEditor.apply();
    }

    /**
     *  Display an alert dialog with a single button saying Ok.
     *
     * @param message  the string storing the message of the alert dialog to be created
     */
    private void createOkAlertDialog(String message) {

        // Build an alert dialog using the title and message
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        // Get the AlertDialog from create() and show it
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
