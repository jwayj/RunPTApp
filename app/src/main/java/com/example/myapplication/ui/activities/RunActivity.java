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
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

public class RunActivity extends AppCompatActivity {
    private FrameLayout statsOverlay;
    private ImageButton btnCloseStats;
    private AppCompatImageButton btnToggle;
    private AppCompatButton btnEndRun;
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
            getSupportActionBar().setDisplayShowTitleEnabled(false);  // 필요 시 타이틀 숨기기
        }

        // ── 통계 오버레이 세팅 ──
        statsOverlay = findViewById(R.id.stats_overlay);
        // 메뉴 클릭 시 오버레이 보이기
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_stats) {
                statsOverlay.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
        // 오버레이 닫기 버튼
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
        btnToggle = findViewById(R.id.btnTogglePause);
        btnToggle.setImageResource(R.drawable.ic_pause);
        btnEndRun=findViewById(R.id.btnEndRun);
        btnToggle.setOnClickListener(v -> {
            long now = SystemClock.elapsedRealtime();
            if (!isPaused) {
                // ▶ 일시정지
                timerHandler.removeCallbacks(timerRunnable);
                pausedOffset = now - startTimeMillis;
                btnToggle.setImageResource(R.drawable.ic_play);
            } else {
                // ▶ 재생
                startTimeMillis = now - pausedOffset;
                timerHandler.post(timerRunnable);
                btnToggle.setImageResource(R.drawable.ic_pause);
            }
            isPaused = !isPaused;
        });

        btnEndRun.setOnClickListener(v -> {
            // ▶ 타이머 멈추고 피드백 화면으로 이동
            timerHandler.removeCallbacks(timerRunnable);
            Intent i = new Intent(this, FeedbackActivity.class);
            i.putExtra("elapsedTime", SystemClock.elapsedRealtime() - startTimeMillis);
            startActivity(i);
            finish();
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // res/menu/run_menu.xml 을 인플레이트
        getMenuInflater().inflate(R.menu.run_menu, menu);
        View actionView = menu.findItem(R.id.action_stats).getActionView();
        actionView.setOnClickListener(v -> {
            // 여기에 통계 페이지 열기 로직
            statsOverlay.setVisibility(View.VISIBLE);
        });
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티 종료 시 핸들러 콜백 제거
        timerHandler.removeCallbacks(timerRunnable);
    }

}
