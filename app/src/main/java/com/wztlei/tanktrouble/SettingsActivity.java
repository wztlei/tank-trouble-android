package com.wztlei.tanktrouble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    private FirestoreUserUtils mFirestoreUserUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mFirestoreUserUtils = new FirestoreUserUtils(this);
        mFirestoreUserUtils.storeUserId();
        mFirestoreUserUtils.setUsername();
    }

    public void onClickSaveButton(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void onClickCancelButton(View view) {
        startActivity(new Intent(this, MainActivity.class));

    }

    public void onClickRandomizeButton(View view) {
        mFirestoreUserUtils.setRandomUsername();
    }

}
