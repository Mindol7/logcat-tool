package com.example.logcat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MonitoringService extends Service {

    private static final String CHANNEL_ID = "MonitoringServiceChannel";
    private Uri logFileUri;
    private BroadcastReceiver timeChangeReceiver;
    private final Handler handler = new Handler();
    private long lastCheckedTime = System.currentTimeMillis();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MonitoringService", "Service started.");

        createNotificationChannel();
        Notification notification = getNotification();
        startForeground(1, notification);

        // Initialize log file
        initializeLogFile();

        // Monitor anti-forensic actions
        monitorAntiForensicActions();
    }

    private void initializeLogFile() {
        ContentResolver resolver = getContentResolver();
        String fileName = "NonVolatile_LogFile.txt";
        String relativePath = "Documents/Logs";

        // Ensure the directory exists
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);

            logFileUri = resolver.insert(MediaStore.Files.getContentUri("external"), values);
            if (logFileUri == null) {
                Toast.makeText(this, "Failed to create log file", Toast.LENGTH_LONG).show();
            }
        } else {
            // For Android versions below Q, create the directory manually
            java.io.File logDir = new java.io.File(getExternalFilesDir(null), relativePath);
            if (!logDir.exists()) {
                boolean dirCreated = logDir.mkdirs();
                if (!dirCreated) {
                    Toast.makeText(this, "Failed to create Logs directory", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            java.io.File logFile = new java.io.File(logDir, fileName);
            try {
                if (logFile.createNewFile()) {
                    logFileUri = Uri.fromFile(logFile);
                } else {
                    Toast.makeText(this, "Failed to create log file", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating log file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isLogFileExists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Check if the log file exists using ContentResolver
            ContentResolver resolver = getContentResolver();
            try (OutputStream outputStream = resolver.openOutputStream(logFileUri, "r")) {
                return outputStream != null;
            } catch (IOException e) {
                return false;
            }
        } else {
            // Check for older Android versions
            java.io.File logDir = new java.io.File(getExternalFilesDir(null), "Documents/Logs");
            java.io.File logFile = new java.io.File(logDir, "NonVolatile_LogFile.txt");
            return logFile.exists();
        }
    }

    private void appendToLogFile(String content) {
        // Check if log file exists, recreate if necessary
        if (logFileUri == null || !isLogFileExists()) {
            initializeLogFile();
        }

        if (logFileUri == null) {
            Toast.makeText(this, "Log file not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentResolver resolver = getContentResolver();
        try (OutputStream outputStream = resolver.openOutputStream(logFileUri, "wa")) {
            if (outputStream != null) {
                outputStream.write(content.getBytes());
                Toast.makeText(this, "Log updated", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating log: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void monitorAntiForensicActions() {
        timeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action)) {
                    // Check if auto time setting is enabled
                    boolean isAutoTimeEnabled = android.provider.Settings.Global.getInt(
                            getContentResolver(),
                            android.provider.Settings.Global.AUTO_TIME,
                            0
                    ) == 1;
                    String logMessage = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
                            + " Anti-forensic event detected: " + action + "\n";
                    logMessage += new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) +
                            " SystemClockTime: Setting time of day to sec=" + System.currentTimeMillis() + "\n";

                    logMessage += new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) +
                            " Auto time setting enabled: " + isAutoTimeEnabled + "\n";

                    logMessage += new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) +
                            " Before System Time : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(lastCheckedTime) + "\n";

                    // Log the event
                    appendToLogFile(logMessage);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(timeChangeReceiver, filter);

        // Schedule periodic time check
        schedulePeriodicTimeCheck();
    }

    private void schedulePeriodicTimeCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                lastCheckedTime = currentTime;
                handler.postDelayed(this, 1000); // Re-run every second
            }
        }, 1000);
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Monitoring Service")
                .setContentText("Monitoring anti-forensic actions...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Monitoring Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timeChangeReceiver != null) {
            unregisterReceiver(timeChangeReceiver);
        }
        handler.removeCallbacksAndMessages(null); // Stop periodic checks
        Log.d("MonitoringService", "Service stopped.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
