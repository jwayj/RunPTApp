package com.example.myapplication.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

public class RunActivity extends AppCompatActivity {
    private FrameLayout statsOverlay;
    private ImageButton btnCloseStats;
    private AppCompatImageButton btnTogglePause;
    private AppCompatImageButton btnEndRun;      // ← AppCompatImageButton 으로 변경
    private TextView tvStatTime;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTimeMillis;
    private long pausedOffset = 0L;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        // ── 툴바 세팅 ──
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // ── 통계 오버레이 ──
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
        runWebView.loadUrl("http://192.168.123.5:4567/maponly.html");

        // ── 타이머 세팅 ──
        tvStatTime   = findViewById(R.id.tvStatTime);
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long now     = SystemClock.elapsedRealtime();
                long elapsed = now - startTimeMillis;
                int hours   = (int) (elapsed / 3600000);
                int minutes = (int) ((elapsed % 3600000) / 60000);
                int seconds = (int) ((elapsed % 60000) / 1000);
                tvStatTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        startTimeMillis = SystemClock.elapsedRealtime();
        timerHandler.post(timerRunnable);

        // ── 버튼 세팅 ──
        btnTogglePause = findViewById(R.id.btnTogglePause);
        btnTogglePause.setImageResource(R.drawable.ic_pause);
        btnTogglePause.setOnClickListener(v -> {
            long now = SystemClock.elapsedRealtime();
            if (!isPaused) {
                // ▶ 일시정지
                timerHandler.removeCallbacks(timerRunnable);
                pausedOffset = now - startTimeMillis;
                btnTogglePause.setImageResource(R.drawable.ic_play);
            } else {
                // ▶ 재생
                startTimeMillis = now - pausedOffset;
                timerHandler.post(timerRunnable);
                btnTogglePause.setImageResource(R.drawable.ic_pause);
            }
            isPaused = !isPaused;
        });

        // ★ btnEndRun 도 ImageButton 계열로 선언했으니 아래처럼 초기화
        btnEndRun = findViewById(R.id.btnEndRun);
        // (선택) 아이콘 초기 상태 지정
        btnEndRun.setImageResource(R.drawable.ic_stop);
        btnEndRun.setOnClickListener(v -> {
            // 타이머 멈추고 다음 화면으로
            timerHandler.removeCallbacks(timerRunnable);
            Intent i = new Intent(this, FeedbackActivity.class);
            i.putExtra("elapsedTime", SystemClock.elapsedRealtime() - startTimeMillis);
            startActivity(i);
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.run_menu, menu);
        View actionView = menu.findItem(R.id.action_stats).getActionView();
        actionView.setOnClickListener(v -> statsOverlay.setVisibility(View.VISIBLE));
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}
