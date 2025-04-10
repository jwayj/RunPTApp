package com.example.myapplication.ui.popups;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.routing_module.RoutingCore;
import com.example.myapplication.R;

public class RoutingForegroundService extends Service {
    private static final String CHANNEL_ID = "RoutingServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 인텐트에서 좌표 값 추출
        Bundle extras = intent.getExtras();
        if (extras != null) {
            double distance = extras.getDouble("distance");
            double startLat = extras.getDouble("startLat");
            double startLon = extras.getDouble("startLon");
            double endLat = extras.getDouble("endLat");
            double endLon = extras.getDouble("endLon");

            // 백그라운드 스레드에서 라우팅 실행
            new Thread(() -> {
                RoutingCore.calculateRoute(distance, startLat, startLon, endLat, endLon);
            }).start();
        }
        return START_STICKY;
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("경로 탐색 중")
                .setContentText("백그라운드에서 경로를 계산하고 있습니다.")
                .setSmallIcon(R.drawable.logo)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "라우팅 서비스",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //RoutingCore.stopContinuousRouting();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

