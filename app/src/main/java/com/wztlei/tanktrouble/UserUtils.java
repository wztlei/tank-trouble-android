package com.wztlei.tanktrouble;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class UserUtils {
    private static DatabaseReference sDatabase;
    private static SharedPreferences sSharedPref;
    private static String[] sAdjectiveList;
    private static String[] sNounList;
    private static String sUsername;
    private static String sUserId;

    private static final String USERS_KEY = Globals.USERS_KEY;
    private static final String USER_ID_KEY = Globals.USER_ID_KEY;
    private static final String USERNAME_KEY = Globals.USERNAME_KEY;
    private static final String TAG = "WL: DatabaseUtils";

    public static void initialize(Activity activity) {
        sAdjectiveList = activity.getResources().getStringArray(R.array.adjective_list);
        sNounList = activity.getResources().getStringArray(R.array.noun_list);
        sSharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        sUserId = sSharedPref.getString(USER_ID_KEY, "");
        sUsername = sSharedPref.getString(USERNAME_KEY, "");
        sDatabase = FirebaseDatabase.getInstance().getReference();
        setUsername(sUsername);
        Log.d(TAG, "sUserId=" + sUserId);
        Log.d(TAG, "sUsername=" + sUsername);
    }




    public static void setUsername(String newUsername){
        if (sUserId.length() == 0 || sUsername.length() == 0) {
            String randomUsername = generateRandomUsername();
            setFirstUsername(randomUsername);
        } else {
            updateUsername(newUsername);
        }
    }

    private static void setFirstUsername(final String firstUsername) {
        final DatabaseReference mUserDataRef = sDatabase.child(USERS_KEY).push();

        mUserDataRef.child(USERNAME_KEY)
                .setValue(firstUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sUserId = mUserDataRef.getKey();
                        sUsername = firstUsername;
                        putStringInPrefs(USER_ID_KEY, sUserId);
                        putStringInPrefs(USERNAME_KEY, firstUsername);
                        Log.d(TAG, "added new user with sUserId=" + sUserId
                                + " and sUsername=" + sUsername);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to add user in updateUsername");
                    }
                });
    }

    private static void updateUsername(final String newUsername) {
        sDatabase.child(USERS_KEY)
                .child(sUserId)
                .child(USERNAME_KEY)
                .setValue(newUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sUsername = newUsername;
                        putStringInPrefs(USERNAME_KEY, newUsername);
                        Log.d(TAG, "updated new username for sUserId=" + sUserId
                                + " with sUsername=" + sUsername);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to update username in updateUsername");
                    }
                });
    }

    /**
     * Concatenates two random adjectives from sAdjectiveList and one random noun
     * from sNounList to produce a random username.
     *
     * @return  a string storing the randomly generated username
     */
    public static String generateRandomUsername() {

        Random random = new Random();

        // Get two adjectives and one noun
        String adjective1 = sAdjectiveList[random.nextInt(sAdjectiveList.length)];
        String adjective2 = sAdjectiveList[random.nextInt(sAdjectiveList.length)];
        String noun = sNounList[random.nextInt(sNounList.length)];

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
    private static void putStringInPrefs (String key, String value) {
        SharedPreferences.Editor sSharedPrefEditor = sSharedPref.edit();
        sSharedPrefEditor.putString(key, value);
        sSharedPrefEditor.apply();
    }

    public static String getUsername() {
        return sUsername;
    }

    
}
