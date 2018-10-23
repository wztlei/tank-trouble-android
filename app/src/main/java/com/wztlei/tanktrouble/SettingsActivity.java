package com.wztlei.tanktrouble;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mSharedPref;
    private FirebaseFirestore mFirestore;
    private String[] mAdjectiveList;
    private String[] mNounList;
    private EditText mEditUsername;
    private String mUserId;
    private String mUsername;
    //private Context mContext;

    private static final String sTag = "WL: SettingsActivity";
    private static final String sUsersKey = "users";
    private static final String sUsernameKey = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //mContext = getApplicationContext();
        mAdjectiveList = getResources().getStringArray(R.array.adjective_list);
        mNounList = getResources().getStringArray(R.array.noun_list);


        mFirestore = FirebaseFirestore.getInstance();
        mSharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mEditUsername = findViewById(R.id.edit_username);
        mUsername = mSharedPref.getString(sUsernameKey, "");
        mUserId = "";

        setUserId();
        setUsername();
    }

    private void setUsernameWithUserId() {
        mFirestore.collection(sUsersKey).document(mUserId)
                .update(sUsernameKey, mUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mEditUsername.setText(mUsername);
                        putStringInPrefs (sUsernameKey, mUsername);
                        Log.d(sTag, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(sTag, "Error updating document", e);
                    }
                });
    }

    private void checkForDuplicateUsernames() {
        mFirestore.collection(sUsersKey)
                .whereEqualTo(sUsernameKey, mUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            List<DocumentSnapshot> userDocuments = querySnapshot.getDocuments();

                            // Check the user document's size
                            switch (userDocuments.size()) {
                                // No users with the user name, mUsername
                                case 0:
                                    if (mUserId.length() == 0) {
                                        mEditUsername.setText(mUsername);
                                        putStringInPrefs (sUsernameKey, mUsername);
                                        addFirestoreUser();
                                    }
                                    else {
                                        setUsernameWithUserId();
                                    }

                                    break;
                                // One user has the username, mUsername
                                case 1:
                                    if (mUserId.length() > 0) {
                                        createOkAlertDialog("Username taken",
                                                "Please try again.");
                                    }

                                    Log.d(sTag,"setUsernameWithoutUserId");
                                    break;
                                    // 2+ users have the username, mUsername
                                default:
                                    createOkAlertDialog("This should not happen.",
                                            "Two or more users have the same username.");
                                    Log.e(sTag, "ERROR: 2+ users have the same username." +
                                            "setUsernameWithoutUserId");
                                    break;
                            }
                        } else {
                            createOkAlertDialog("", "Task failed.");
                        }
                    }
                });
    }


    /**
     * Sets the username of the EditText field with data from SharedPreferences variable
     * or generates a random one if a username was not previously set.
     */
    private void setUsername() {

        if (mUsername.length() == 0) {
            mUsername = generateRandomUsername();
        }

        if (mUserId.length() == 0) {
            checkForDuplicateUsernames();
        } else {
            setUsernameWithUserId();
        }
    }

    private void putStringInPrefs (String key, String value) {
        SharedPreferences.Editor mSharedPrefEditor = mSharedPref.edit();
        mSharedPrefEditor.putString(key, value);
        mSharedPrefEditor.apply();
    }

    private void setUserId() {
        if (mUsername.length() == 0) {
            mUserId = "";
            return;
        }

        mFirestore.collection(sUsersKey)
                .whereEqualTo(sUsernameKey, mUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            List<DocumentSnapshot> userDocuments = querySnapshot.getDocuments();

                            if (userDocuments.size() == 1) {
                                mUserId = userDocuments.get(0).getId();
                                Log.d(sTag, "User Id is set" + mUserId);
                            } else if (userDocuments.size() > 1) {
                                createOkAlertDialog("This should not happen.",
                                                 "Two or more users have the same username." +
                                                         "setUserId");
                                Log.e(sTag, "ERROR: Two or more users have the same username.");
                            }
                        } else {
                            createOkAlertDialog("", "Task failed.");
                        }
                    }
                });
    }

    private void addFirestoreUser() {
        Map<String, Object> user = new HashMap<>();
        user.put(sUsernameKey, mUsername);

        // Add a new document with a generated ID
        mFirestore.collection(sUsersKey)
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(sTag, "Add a user with username: " + mUsername);
                            setUserId();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(sTag, "Error adding document", e);
                        }
                    });
    }

    /**
     *  Display an alert dialog with a single button saying Ok.
     */
    private void createOkAlertDialog(String title, String message) {

        // Build an alert dialog using the title and message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    /**
     *  Saves the user's inputted settings into Firestore.
     */
    public void onClickSaveButton(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }


    public void onClickCancelButton(View view) {
        startActivity(new Intent(this, MainActivity.class));

    }


    private String generateRandomUsername() {

        Random random = new Random();

        // Get two adjectives and one noun
        String adjective1 = mAdjectiveList[random.nextInt(mAdjectiveList.length)];
        String adjective2 = mAdjectiveList[random.nextInt(mAdjectiveList.length)];
        String noun = mNounList[random.nextInt(mNounList.length)];

        // Capitalize each string with the rest of the string being lowercase characters
        adjective1 = adjective1.substring(0, 1).toUpperCase()
                + adjective1.substring(1).toLowerCase();
        adjective2 = adjective2.substring(0, 1).toUpperCase()
                + adjective2.substring(1).toLowerCase();
        noun = noun.substring(0, 1).toUpperCase()
                + noun.substring(1).toLowerCase();

        return adjective1 + adjective2 + noun;
    }


    public void onClickRandomizeButton(View view) {
        mUsername = generateRandomUsername();
        setUsername();
    }

}
