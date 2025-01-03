package com.example.logcat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한 요청
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        // READ_MEDIA_IMAGES 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_READ_MEDIA_IMAGES);
        } else {
            Log.d("MainActivity", "READ_MEDIA_IMAGES permission granted.");
            // 권한이 이미 허용된 경우 추가 작업 수행
            startMonitoringService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "READ_MEDIA_IMAGES permission granted by user.");
                // 권한이 허용되었을 때 수행할 작업
                startMonitoringService();
            } else {
                Log.d("MainActivity", "READ_MEDIA_IMAGES permission denied by user.");
                // 권한이 거부되었을 때 처리할 작업
                Toast.makeText(this, "Permission is required to monitor media changes.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startMonitoringService() {
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        startService(serviceIntent);
        Log.d("MainActivity", "MonitoringService started.");
    }
}
