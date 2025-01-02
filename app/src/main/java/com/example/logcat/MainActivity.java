package com.example.logcat;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 서비스 시작 (앱이 실행될 때 자동 시작)
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        startForegroundService(serviceIntent);
    }
}
