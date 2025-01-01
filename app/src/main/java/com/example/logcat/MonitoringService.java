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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MonitoringService", "Service started.");

        createNotificationChannel();
        Notification notification = getNotification();
        startForeground(1, notification);

        // 모니터링 로직 시작
        initializeLogFile();
        monitorAntiForensicActions();
    }

    private void initializeLogFile() {
        // ContentResolver를 사용하여 MediaStore에 접근
        ContentResolver resolver = getContentResolver();
        String fileName = "NonVolatile_LogFile" + ".txt";

        // 파일 메타데이터 설정
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // 파일 이름
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain"); // 파일 MIME 타입
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Logs"); // 저장 경로

        // MediaStore에 파일 삽입
        logFileUri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

        if (logFileUri == null) {
            Toast.makeText(this, "Failed to create log file", Toast.LENGTH_LONG).show();
        }
    }
    private void monitorAntiForensicActions() {
        timeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action)) {
                    String logMessage = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) +
                                        " Anti-forensic event detected: " + action + "\n";

                    logMessage += new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) +
                                    "SystemClockTime: Setting time of day to sec=" + System.currentTimeMillis() + "\n";

                    appendToLogFile(logMessage);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(timeChangeReceiver, filter);
    }
    private void appendToLogFile(String content) {
        if (logFileUri == null) {
            Toast.makeText(this, "Log file not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentResolver resolver = getContentResolver();
        try (OutputStream outputStream = resolver.openOutputStream(logFileUri, "wa")) {
            if (outputStream != null) {
                outputStream.write(content.getBytes()); // 여기서 로그 작성함
                Toast.makeText(this, "Log updated", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating log: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
        Log.d("MonitoringService", "Service stopped.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}