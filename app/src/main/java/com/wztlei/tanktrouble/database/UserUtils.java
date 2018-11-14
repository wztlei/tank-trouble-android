package com.wztlei.tanktrouble.database;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wztlei.tanktrouble.Globals;
import com.wztlei.tanktrouble.R;

import java.util.Random;

public class UserUtils {
    private DatabaseReference mDatabase;
    private Activity mActivity;
    private SharedPreferences mSharedPref;
    private EditText mEditUsername;
    private String[] mAdjectiveList;
    private String[] mNounList;
    private String mUsername;
    private String mUserId;

    private static final String USERS_KEY = Globals.USERS_KEY;
    private static final String USER_ID_KEY = Globals.USER_ID_KEY;
    private static final String USERNAME_KEY = Globals.USERNAME_KEY;
    private static final String TAG = "WL: DatabaseUtils";

    public UserUtils(Activity activity){

        mActivity = activity;
        mAdjectiveList = activity.getResources().getStringArray(R.array.adjective_list);
        mNounList = activity.getResources().getStringArray(R.array.noun_list);
        mEditUsername = activity.findViewById(R.id.edit_username);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        mUserId = mSharedPref.getString(USER_ID_KEY, "");
        mUsername = mSharedPref.getString(USERNAME_KEY, "");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setNewUsername(mUsername);
        Log.d(TAG, "mUserId=" + mUserId);
        Log.d(TAG, "mUsername=" + mUsername);

    }

    public void setRandomUsername(){
        String randomUsername = generateRandomUsername();
        setNewUsername(randomUsername);
    }

    private void setNewUsername(final String newUsername){
        if (mUserId.length() == 0 || mUsername.length() == 0) {
            setFirstUsername(newUsername);
        } else {
            updateUsername(newUsername);
        }
    }

    private void setFirstUsername(final String firstUsername) {
        final DatabaseReference mUserDataRef = mDatabase.child(USERS_KEY).push();

        mUserDataRef.child(USERNAME_KEY)
                .setValue(firstUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mUserId = mUserDataRef.getKey();
                        mUsername = firstUsername;
                        putStringInPrefs(USER_ID_KEY, mUserId);
                        putStringInPrefs(USERNAME_KEY, firstUsername);
                        setEditUsername();
                        Log.d(TAG, "added new user with mUserId=" + mUserId
                                + " and mUsername=" + mUsername);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to add user in updateUsername");
                    }
                });
    }

    private void updateUsername(final String newUsername) {
        mDatabase.child(USERS_KEY)
                .child(mUserId)
                .child(USERNAME_KEY)
                .setValue(newUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mUsername = newUsername;
                        putStringInPrefs(USERNAME_KEY, newUsername);
                        setEditUsername();
                        Log.d(TAG, "updated new username for mUserId=" + mUserId
                                + " with mUsername=" + mUsername);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to update username in updateUsername");
                    }
                });
    }

    private void setEditUsername() {
        if (mEditUsername != null) {
            mEditUsername.setText(mUsername);
        }
    }

    /**
     * Concatenates two random adjectives from mAdjectiveList and one random noun
     * from mNounList to produce a random username.
     *
     * @return  a string storing the randomly generated username
     */
    public String generateRandomUsername() {

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
