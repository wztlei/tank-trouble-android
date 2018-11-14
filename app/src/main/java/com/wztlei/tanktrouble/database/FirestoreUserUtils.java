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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wztlei.tanktrouble.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FirestoreUserUtils {

    private SharedPreferences mSharedPref;
    private FirebaseFirestore mFirestore;
    private Activity mActivity;
    private EditText mEditUsername;
    private String[] mAdjectiveList;
    private String[] mNounList;
    private String mUserId;
    private String mUsername;

    private static final String TAG = "WL: FirestoreUserUtils";
    private static final String USERS_KEY = "users";
    private static final String USERNAME_KEY = "username";
    private static final String USER_ID_KEY = "userId";

    /**
     * Constructor function for the FirestoreUserUtils class.
     *
     * @param activity  the activity in which the FirestoreUserUtils class is instantiated
     */
    public FirestoreUserUtils(Activity activity) {
        mActivity = activity;
        mAdjectiveList = activity.getResources().getStringArray(R.array.adjective_list);
        mNounList = activity.getResources().getStringArray(R.array.noun_list);
        mEditUsername = activity.findViewById(R.id.edit_username);
        mFirestore = FirebaseFirestore.getInstance();
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        mUsername = mSharedPref.getString(USERNAME_KEY, "");
        mUserId = mSharedPref.getString(USER_ID_KEY, "");
        Log.d(TAG, "mUsername=" + mUsername);
        Log.d(TAG, "mUserId=" + mUserId);
    }
    
    /**
     * Sets a new random username and calls setUsername to store the new data accordingly.
     */
    public void setRandomUsername() {
        mUsername = "";
        setUsername();
    }
    
    /**
     * Generates and sets a random username if a username was not previously set and then stores
     * the username accordingly depending on the existence of a user ID.
     */
    public void setUsername() {
        
        // Generates a new random username if the current username is empty
        if (mUsername.length() == 0) {
            mUsername = generateRandomUsername();
        }

        Log.d(TAG, "mUsername=" + mUsername);
        Log.d(TAG, "mUserId=" + mUserId);

        // Set the text of the EditText so the user sees that the username changed
        if (mEditUsername != null) {
            mEditUsername.setText(mUsername);
        }

        putStringInPrefs(USERNAME_KEY, mUsername);

        // Check whether the user has an ID stored and store the username accordingly
        if (mUserId.length() == 0) {
            Log.d(TAG, "addFirestoreUser()");
            addFirestoreUser();
        } else {
            Log.d(TAG, "updateUserName()");
            updateUserName();
        }
    }

    /**
     * Updates the username property of a user in Firestore and SharedPreferences with a new
     * username, mUsername.
     */
    private void updateUserName() {
        mFirestore.collection(USERS_KEY)
                .document(mUserId)
                .update(USERNAME_KEY, mUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Put the string in the SharedPreferences object
                        Log.d(TAG, "User document successfully updated with username " +
                                mUsername);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        createOkAlertDialog("Failed to updateUserName");
                        addFirestoreUser();
                        Log.w(TAG, "User document failed to update with username " +
                                mUsername);
                        e.printStackTrace();
                    }
                });
    }


    /**
     * Adds a new Firestore document to the "users" collection with the username mUsername and
     * stores the user ID.
     */
    private void addFirestoreUser() {
        
        // Create a new map storing the data for a new user with a username mUsername
        Map<String, Object> user = new HashMap<>();
        user.put(USERNAME_KEY, mUsername);

        // Add a new document to the "users" collection with the data in the map user
        mFirestore.collection(USERS_KEY)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // A document in Firestore with a randomly created ID has the user's data
                        // so we need to store the randomly created id associated with the user
                        mUserId = documentReference.getId();
                        putStringInPrefs(USER_ID_KEY, mUserId);
                        Log.d(TAG, "Successfully added a user with username: " + mUsername);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        createOkAlertDialog("Failed to addFirestoreUser.");
                        Log.w(TAG, "Failed to add a user with username: " + mUsername, e);
                    }
                });
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
