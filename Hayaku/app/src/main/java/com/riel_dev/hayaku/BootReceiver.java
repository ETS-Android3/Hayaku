package com.riel_dev.hayaku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(CustomPreferenceManager.getBoolean(context, "bootNotificationSwitch")) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                Intent bootIntent = new Intent(context, SendTweetService.class);
                bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(bootIntent);
            }
        }
    }
}
