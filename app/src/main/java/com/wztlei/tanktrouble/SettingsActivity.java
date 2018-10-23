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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mSharedPref;
    private FirebaseFirestore mFirestore;
    private ArrayList<String> mAdjectiveList;
    private ArrayList<String> mNounList;
    private EditText mEditUsername;
    private SharedPreferences.Editor mSharedPrefEditor;
    private String mUserId;
    private String mUsername;
    private Context mContext;

    private static final String sTag = "WL: SettingsActivity";
    private static final String sUserIdKey = "userId";
    private static final String sUsernameKey = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_settings);
        mFirestore = FirebaseFirestore.getInstance();
        mSharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mEditUsername = findViewById(R.id.edit_username);
        setUserId();

        mUsername = mSharedPref.getString(sUsernameKey, "");
        setUsername();
    }


    private void setUsername () {

        new Thread(new Runnable() {
            public void run() {
                if (mAdjectiveList == null || mNounList == null) {
                    Log.d(sTag, "Read files");
                    // Read adjective_list.txt
                    InputStream inputStream = mContext.getResources().openRawResource(R.raw.adjective_list);
                    Scanner scanner = new Scanner(inputStream);
                    mAdjectiveList = new ArrayList<>();

                    while(scanner.hasNextLine()) {
                        String word = scanner.nextLine();
                        mAdjectiveList.add(word);
                    }

                    // Read noun_list.txt
                    inputStream = mContext.getResources().openRawResource(R.raw.noun_list);
                    scanner = new Scanner(inputStream);
                    mNounList = new ArrayList<>();

                    while(scanner.hasNextLine()) {
                        String word = scanner.nextLine();
                        mNounList.add(word);
                    }
                }

                Log.d(sTag, "inside setUsername");
                 mEditUsername.post(new Runnable() {

                    public void run() {
                        Log.d(sTag, "call setEditTextUsername");
                        setEditTextUsername();
                    }
                });
            }
        }).start();
    }

    /**
     * Sets the username of the EditText field with data from SharedPreferences variable
     * or generates a random one if a username was not previously set.
     */
    private void setEditTextUsername () {
        mUsername = mSharedPref.getString(sUsernameKey, "");
        Log.d(sTag, "set username");

        if (mUsername.length() == 0) {
            Log.d(sTag, "Randomize username");
            mUsername = generateRandomUsername();
        }

        mEditUsername.setText(mUsername);
        mSharedPrefEditor = mSharedPref.edit();
        mSharedPrefEditor.putString(sUsernameKey, mUsername);
        mSharedPrefEditor.apply();

        addFirestoreUser();
    }

    private void setUserId() {
        mFirestore.collection("users")
                .whereEqualTo(sUsernameKey, mUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                            if (documents.size() == 0) {
                                addFirestoreUser();
                            } else if (documents.size() == 1) {
                                mUserId = documents.get(0).getId();
                            } else {
                                createOkAlertDialog("This should not happen.",
                                                 "Two or more users have the same username.");
                                Log.e(sTag, "ERROR: Two or more users have the same username.");
                            }
                        } else {
                            createOkAlertDialog("", "Task failed.");
                        }
                    }
                });
    }

    private void addFirestoreUser () {
        Map<String, Object> user = new HashMap<>();
        user.put(sUsernameKey, mUsername);

        // Add a new document with a generated ID
        mFirestore.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(sTag, "Add a user with username: " + mUsername);
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

    private String generateRandomUsername () {
        if (mAdjectiveList == null || mNounList == null) {
            return "";
        }
        else {
            Random random = new Random();

            // Get two adjectives and one noun
            String adjective1 = mAdjectiveList.get(random.nextInt(mAdjectiveList.size()));
            String adjective2 = mAdjectiveList.get(random.nextInt(mAdjectiveList.size()));
            String noun = mNounList.get(random.nextInt(mNounList.size()));

            // Capitalize each string with the rest of the string being lowercase characters
            adjective1 = adjective1.substring(0, 1).toUpperCase()
                    + adjective1.substring(1).toLowerCase();
            adjective2 = adjective2.substring(0, 1).toUpperCase()
                    + adjective2.substring(1).toLowerCase();
            noun = noun.substring(0, 1).toUpperCase()
                    + noun.substring(1).toLowerCase();

            return adjective1 + adjective2 + noun;
        }

    }

    /**
     *  Saves the user's inputted settings into Firestore.
     */
    public void onClickSaveButton(View view) {

    }

    private void setFirestoreUsername (String username) {
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);

        // Add a new document with a generated ID
        Task<DocumentReference> documentReferenceTask = mFirestore.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(sTag, "User updated successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(sTag, "User failed to add", e);
                    }
                });
    }

    public void onClickCancelButton(View view) {
        startActivity(new Intent(this, MainActivity.class));

    }

    public void onClickRandomizeButton(View view) {
        mSharedPrefEditor = mSharedPref.edit();
        mSharedPrefEditor.putString(sUsernameKey, "");
        mSharedPrefEditor.apply();
        mUsername = "";
        setUsername();
    }
}
