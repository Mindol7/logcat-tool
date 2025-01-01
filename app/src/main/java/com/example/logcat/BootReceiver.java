package com.example.logcat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Log.d("BootReceiver", "Device boot completed. Starting MainActivity");

            Intent serviceIntent = new Intent(context, MonitoringService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
