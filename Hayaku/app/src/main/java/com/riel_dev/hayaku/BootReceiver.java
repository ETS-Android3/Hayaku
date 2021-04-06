package com.riel_dev.hayaku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.logging.Handler;

public class BootReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if(CustomPreferenceManager.getBoolean(context, "bootNotificationSwitch")) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                Intent bootIntent = new Intent(context, SendTweetService.class);
                bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    context.startForegroundService(bootIntent);
                }
                else{
                    context.startService(bootIntent);
                }
            }
        }
    }
}
