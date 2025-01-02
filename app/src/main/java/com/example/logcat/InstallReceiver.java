package com.example.logcat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class InstallReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();
        if(Intent.ACTION_PACKAGE_ADDED.equals(action) || Intent.ACTION_PACKAGE_REPLACED.equals(action)){
            Log.d("InstallReceiver", "App installed or updated. Starting MonitoringService.");

            Intent serviceIntent = new Intent(context, MonitoringService.class);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                context.startForegroundService(serviceIntent);
            }
            else{
                context.startService(serviceIntent);
            }
        }
    }

}
