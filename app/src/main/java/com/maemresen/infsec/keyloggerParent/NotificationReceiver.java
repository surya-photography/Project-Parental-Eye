package com.maemresen.infsec.keyloggerParent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the fragment tag from the intent
        String fragmentTag = intent.getStringExtra("fragmentTag");
        
        // Show the notification
        Intent showNotificationIntent = new Intent(context, CaughtUsingBadWordsFragment.class);
        showNotificationIntent.putExtra("fragmentTag", fragmentTag);
        context.startActivity(showNotificationIntent);
    }
}