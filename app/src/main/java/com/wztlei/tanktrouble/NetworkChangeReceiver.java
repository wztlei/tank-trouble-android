package com.wztlei.tanktrouble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "WL/NetworkChange...";

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isConnected = intent.getBooleanExtra
                (ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

        if(isConnected){
            Log.d(TAG, "Connected to network");
        }else{
            Log.d(TAG, "Connection Error");

            // Build an alert dialog using the title and message
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Connection Error")
                    .setMessage("Check your internet connection and try again.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });

            // Get the AlertDialog from create() and show it
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
