package com.royalenfield.recieverapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MessageReceiver extends BroadcastReceiver {
    public static final String CUSTOM_ACTION = "com.royalenfield.evcansim.CUSTOM_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(CUSTOM_ACTION)) {
                String message = intent.getStringExtra("message");
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
