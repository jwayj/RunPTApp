package com.example.myapplication.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import android.webkit.GeolocationPermissions;
import androidx.core.content.ContextCompat; // ContextCompat
import android.webkit.WebChromeClient;      // WebChromeClient


public class RunActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private FrameLayout statsOverlay;
    private ImageButton btnCloseStats;
    private AppCompatImageButton btnTogglePause;
    private AppCompatImageButton btnEndRun;
    private TextView tvStatTime;
    private TextView tvDistance;
    private TextView tvPace;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTimeMillis;
    private long pausedOffset = 0L;
    private boolean isPaused = false;

    private DisMeasurement disMeasurement;
    //geojson파일 ID
    private String geoJsonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // WebViewActivity → Intent 로 넘어온 ID
        geoJsonId = getIntent().getStringExtra("GEOJSON_ID");
        Log.d("RunActivity", "geoJsonId = " + geoJsonId);

        // ── 툴바 설정 ──
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // ── 통계 오버레이 설정 ──
        statsOverlay = findViewById(R.id.stats_overlay);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_stats) {
                statsOverlay.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
        btnCloseStats = findViewById(R.id.btnCloseStats);
        btnCloseStats.setOnClickListener(v -> statsOverlay.setVisibility(View.GONE));

        // ── WebView 초기화 ──
        WebView runWebView = findViewById(R.id.runWebView);
        WebSettings ws = runWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        runWebView.setWebViewClient(new WebViewClient());
        ws.setGeolocationEnabled(true);

        runWebView.setWebViewClient(new WebViewClient());
        runWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        runWebView.loadUrl("https://f7c3-222-110-177-88.ngrok-free.app/maponly.html");

        // ── 시간, 거리, 페이스 TextView 설정 ──
        tvStatTime = findViewById(R.id.tvStatTime);
        tvDistance = findViewById(R.id.tvStatDistance);
        tvPace = findViewById(R.id.tvStatPace);

        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long now = SystemClock.elapsedRealtime();
                long elapsed = now - startTimeMillis;
                int hours = (int) (elapsed / 3600000);
                int minutes = (int) ((elapsed % 3600000) / 60000);
                int seconds = (int) ((elapsed % 60000) / 1000);
                tvStatTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        startTimeMillis = SystemClock.elapsedRealtime();
        timerHandler.post(timerRunnable);

        // ── 거리 체크 및 페이스 계산 시작 ──
        if (checkLocationPermission()) {
            initMeasurement();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }

        // ── 일시정지/재시작 버튼 ──
        btnTogglePause = findViewById(R.id.btnTogglePause);
        btnTogglePause.setImageResource(R.drawable.ic_pause);
        btnTogglePause.setOnClickListener(v -> {
            long now = SystemClock.elapsedRealtime();
            if (!isPaused) {
                timerHandler.removeCallbacks(timerRunnable);
                pausedOffset = now - startTimeMillis;
                btnTogglePause.setImageResource(R.drawable.ic_play);
            } else {
                startTimeMillis = now - pausedOffset;
                timerHandler.post(timerRunnable);
                btnTogglePause.setImageResource(R.drawable.ic_pause);
            }
            isPaused = !isPaused;
        });

        // ── 종료 버튼 ──
        btnEndRun = findViewById(R.id.btnEndRun);
        btnEndRun.setImageResource(R.drawable.ic_stop);
        btnEndRun.setOnClickListener(v -> {
            timerHandler.removeCallbacks(timerRunnable);
            if (disMeasurement != null) disMeasurement.stop();
            // Firestore에 러닝 기록 저장
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference userRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid);
            Map<String, Object> runData = new HashMap<>();
            runData.put("timestamp", FieldValue.serverTimestamp());
            runData.put("distanceKm", disMeasurement.getDisplayDistanceKm());
            runData.put("durationMillis", SystemClock.elapsedRealtime() - startTimeMillis);
            runData.put("paceMinPerKm", (SystemClock.elapsedRealtime() - startTimeMillis)/disMeasurement.getDisplayDistanceKm());
            runData.put("elevationGain", disMeasurement.getTotalElevationGain());
            runData.put("geojsonID",geoJsonId);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("runs")
                    .add(runData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("RunActivity", "Run saved: " + documentReference.getId());
                            userRef.update(
                                    "totalDistance", FieldValue.increment(disMeasurement.getDisplayDistanceKm()),
                                    "totalElevation", FieldValue.increment(disMeasurement.getTotalElevationGain()),
                                    "totalCount",     FieldValue.increment(1)
                            )
                            .addOnSuccessListener(aVoid -> Log.d("RunActivity", "Totals updated"))
                            .addOnFailureListener(e -> Log.e("RunActivity", "Totals update failed", e));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("RunActivity", "Error saving run", e);
                        }
                    });
            Intent i = new Intent(this, FeedbackActivity.class);
            i.putExtra("elapsedTime", SystemClock.elapsedRealtime() - startTimeMillis);
            i.putExtra("distance", disMeasurement != null ? disMeasurement.getDisplayDistanceKm() : 0f);
            i.putExtra("elevationGain", disMeasurement != null ? disMeasurement.getTotalElevationGain() : 0.0);
            startActivity(i);
            finish();
        });
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void initMeasurement() {
        disMeasurement = new DisMeasurement(this, displayDistance -> runOnUiThread(() -> {
            tvDistance.setText(String.format("%.2f km", displayDistance));
            long elapsed = SystemClock.elapsedRealtime() - startTimeMillis;
            float elapsedMin = elapsed / 60000f;
            if (displayDistance > 0.01f) {
                float pace = elapsedMin / displayDistance;
                tvPace.setText(String.format("%.2f 분/km", pace));
            } else {
                tvPace.setText("페이스: 계산 중...");
            }
        }));
        disMeasurement.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        if (disMeasurement != null) disMeasurement.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.run_menu, menu);
        View actionView = menu.findItem(R.id.action_stats).getActionView();
        actionView.setOnClickListener(v -> statsOverlay.setVisibility(View.VISIBLE));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initMeasurement();
        } else {
            tvDistance.setText("위치 권한 필요");
        }
    }
}
