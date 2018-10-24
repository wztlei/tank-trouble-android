package com.wztlei.tanktrouble;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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

public class FirestoreUserUtils {

    private SharedPreferences mSharedPref;
    private FirebaseFirestore mFirestore;
    private String[] mAdjectiveList;
    private String[] mNounList;
    private EditText mEditUsername;
    private String mUserId;
    private String mUsername;
    private boolean objectJustInitialized;
    private Activity mActivity;

    private static final String sTag = "WL: FirestoreUserUtils";
    private static final String sUsersKey = "users";
    private static final String sUsernameKey = "username";
    private static final String sUserIdKey = "userId";

    /**
     * Constructor function for the FirestoreUserUtils class.
     *
     * @param activity  the activity in which the FirestoreUserUtils class is instantiated
     */
    FirestoreUserUtils(Activity activity) {
        this.mActivity = activity;
        mAdjectiveList = activity.getResources().getStringArray(R.array.adjective_list);
        mNounList = activity.getResources().getStringArray(R.array.noun_list);
        mEditUsername = activity.findViewById(R.id.edit_username);
        mFirestore = FirebaseFirestore.getInstance();
        mSharedPref = activity.getPreferences(Context.MODE_PRIVATE);

        mUsername = mSharedPref.getString(sUsernameKey, "");
        mUserId = mSharedPref.getString(sUserIdKey, "");
        objectJustInitialized = true;
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

        // Check to see if the user has an ID stored and store the username accordingly
        if (mUserId.length() == 0) {
            storeUserNameWithoutUserId();
        } else {
            storeUserNameWithUserId();
        }
    }

    /**
     * Queries Firestore to determine if any users have the new username, mUsername.
     * If possible, it will update and store the new username in Firestore while ensuring that
     * all usernames in Firestore are unique.
     */
    private void storeUserNameWithUserId() {

        // Get all the documents where the username property of the document is mUsername
        mFirestore.collection(sUsersKey)
                .whereEqualTo(sUsernameKey, mUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Get a list of the documents with the username, mUsername
                            List<DocumentSnapshot> userDocuments = task.getResult().getDocuments();

                            // Check the user document's size
                            switch (userDocuments.size()) {
                                // If zero Firebase users have the user name, mUsername
                                case 0:
                                    updateUserNameWithUserId();
                                    break;
                                // If one Firebase user has the username, mUsername
                                case 1:
                                    // Check to see if the ID of the Firebase document
                                    // matches the ID of the current user
                                    if (userDocuments.get(0).getId().equals(mUserId)) {
                                        // The two IDs match so we just update the username
                                        updateUserNameWithUserId();
                                        Log.d(sTag, "One Firebase user with the username" +
                                                "mUsername, but assumed to be current user.");
                                    } else {
                                        // The two IDs don't match so the user tried to change
                                        // their username to that of another user in Firebase

                                        createOkAlertDialog("Username taken",
                                                "Please try again.");

                                        // TODO: Add code to reset mUsername to the old username
                                        // TODO: before the user tried to change the username
                                        // TODO: Add a new member variable - mOldUsername

                                        Log.d(sTag, "One Firebase user with the username" +
                                                "mUsername, so no username is changed.");
                                    }
                                    break;
                                // If 2+ users have the username, mUsername
                                default:
                                    createOkAlertDialog("This should not happen.",
                                            "Two or more users have the same username.");
                                    Log.e(sTag, "ERROR: 2+ users have the same username." +
                                            "storeUserNameWithoutUserId");
                                    break;
                            }
                        } else {
                            createOkAlertDialog("", "Task failed.");
                        }
                    }
                });

    }

    /**
     * Updates the username property of a user in Firestore and SharedPreferences with a new
     * username, mUsername.
     */
    private void updateUserNameWithUserId() {
        mFirestore.collection(sUsersKey).document(mUserId)
                .update(sUsernameKey, mUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Set the text of the EditText so the user sees that the username changed
                        if (mEditUsername != null) {
                            mEditUsername.setText(mUsername);
                        }

                        // Put the string in the SharedPreferences object
                        putStringInPrefs (sUsernameKey, mUsername);
                        Log.d(sTag, "User document successfully updated with username" +
                                mUsername);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(sTag, "User document failed to update with username" +
                                mUsername);
                    }
                });
    }

    /**
     * Stores the username mUsername in the EditText text box, Firestore, and the
     * SharedPreferences object while ensuring that every user has a unique username.
     */
    private void storeUserNameWithoutUserId() {

        // Get all the documents where the username property of the document is mUsername
        mFirestore.collection(sUsersKey)
                .whereEqualTo(sUsernameKey, mUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Get a list of the documents with the username, mUsername
                            List<DocumentSnapshot> userDocuments = task.getResult().getDocuments();

                            // Check the user documents' size
                            switch (userDocuments.size()) {
                                // If zero Firebase users have the user name, mUsername
                                case 0:
                                    if (mEditUsername != null) {
                                        mEditUsername.setText(mUsername);
                                    }

                                    putStringInPrefs (sUsernameKey, mUsername);
                                    addFirestoreUser();
                                    break;
                                // If one Firebase user has the username, mUsername
                                case 1:
                                    // Check to see if the object has just been initialized
                                    if (objectJustInitialized) {
                                        // The FirestoreUserUtils object is just initialized so we
                                        // assume that the Firebase user is the current user since
                                        // it is impossible for the current user to have changed
                                        // their username to a duplicate username in Firestore
                                        // If the user tried to change their username to a
                                        // duplicate username, then objectJustInitialized would be
                                        // false since FirestoreUserUtils was already initialized

                                        putStringInPrefs (sUsernameKey, mUsername);
                                        storeUserId();

                                        if (mEditUsername != null) {
                                            mEditUsername.setText(mUsername);
                                        }

                                        Log.d(sTag, "One Firebase user with the username" +
                                                "mUsername, but assumed to be current user.");
                                    } else {
                                        // The FirestoreUserUtils object is already initialized so
                                        // we assume that the Firebase user is NOT the current user
                                        // since the current user had to have changed their
                                        // username in order for there to be a duplicate

                                        createOkAlertDialog("Username taken",
                                                "Please try again.");

                                        // Generate a new username and set it
                                        mUsername = generateRandomUsername();
                                        setUsername();

                                        Log.d(sTag, "One Firebase user with the username" +
                                                "mUsername, so no username is changed.");
                                    }

                                    break;
                                // If 2+ users have the username, mUsername
                                default:
                                    createOkAlertDialog("This should not happen.",
                                            "Two or more users have the same username.");
                                    Log.e(sTag, "ERROR: 2+ users have the same username." +
                                            "storeUserNameWithoutUserId");
                                    break;
                            }
                        } else {
                            createOkAlertDialog("", "Task failed.");
                        }
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
        user.put(sUsernameKey, mUsername);

        // Add a new document to the "users" collection with the data in the map user
        mFirestore.collection(sUsersKey)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // A document in Firestore with a randomly created ID has the user's data
                        // so we need to store the randomly created id associated with the user
                        storeUserId();
                        Log.d(sTag, "Successfully added a user with username: " + mUsername);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(sTag, "Failed to add a user with username: " + mUsername, e);
                    }
                });
    }

    /**
     * Stores the ID of the document associated with the user in the member variable mUserId
     * and in the SharedPreferences object.
     */
    public void storeUserId() {

        // Check whether the user has a username set yet
        if (mUsername.length() == 0) {
            // Without a username, no Firestore document can store the user's data at this time
            // This means the user has no meaningful ID so we store the empty string
            mUserId = "";
            putStringInPrefs(sUserIdKey, mUserId);

        } else {
            // Query Firestore and get all documents with the username, mUsername
            mFirestore.collection(sUsersKey)
                    .whereEqualTo(sUsernameKey, mUsername)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                // Store the list of documents with the username, mUsername
                                List<DocumentSnapshot> userDocuments =
                                        task.getResult().getDocuments();

                                // Check how many Firestore users have the username, mUsername
                                if (userDocuments.size() == 1) {
                                    // There is only one Firestore user so we assume it is
                                    // the current user and we store the user ID
                                    mUserId = userDocuments.get(0).getId();
                                    putStringInPrefs(sUserIdKey, mUserId);
                                    Log.d(sTag, "User Id is set" + mUserId);

                                } else if (userDocuments.size() > 1) {
                                    // 2+ users have the same username which should never happen
                                    // so we show an alert dialog and log the error
                                    createOkAlertDialog("This should not happen.",
                                            "2+ users have the same username." +
                                                    "storeUserId");
                                    Log.e(sTag, "ERROR: 2+ users have the same username.");
                                }
                            } else {
                                createOkAlertDialog("", "Task failed.");
                            }
                        }
                    });
        }
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
     *  @param title    the string storing the title of the alert dialog to be created
     *  @param message  the string storing the message of the alert dialog to be created
     */
    private void createOkAlertDialog(String title, String message) {

        // Build an alert dialog using the title and message
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        // Get the AlertDialog from create() and show it
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
