package com.example.logcat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startButton);

        // 버튼 클릭 이벤트 설정
        startButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, MonitoringService.class);
            startForegroundService(serviceIntent);
            Toast.makeText(this, "Monitoring started manually.", Toast.LENGTH_SHORT).show();
        });
    }
}