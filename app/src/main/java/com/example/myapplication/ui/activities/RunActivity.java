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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

public class RunActivity extends AppCompatActivity {
    private FrameLayout statsOverlay;
    private ImageButton btnCloseStats;

    // 타이머 관련 변수
    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTimeMillis;
    private Button btnEndRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        // 툴바 세팅
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 뒤로 가기 화살표
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ① 오버레이 뷰 찾기
        statsOverlay = findViewById(R.id.stats_overlay);

        // ② 툴바 메뉴 클릭 리스너
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_stats) {
                statsOverlay.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        // ③ X 버튼 클릭
        ImageButton closeBtn = findViewById(R.id.btnCloseStats);
        closeBtn.setOnClickListener(v -> statsOverlay.setVisibility(View.GONE));

        // WebView 초기화 (기존대로)
        WebView runWebView = findViewById(R.id.runWebView);
        WebSettings ws = runWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        runWebView.setWebViewClient(new WebViewClient());
        runWebView.loadUrl("http://192.168.123.5:4567/maponly.html");

        // ① 타이머 핸들러·러너블 준비
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.elapsedRealtime() - startTimeMillis;
                int hours   = (int) (elapsed / 3600000);
                int minutes = (int) ((elapsed % 3600000) / 60000);
                int seconds = (int) ((elapsed % 60000) / 1000);
                String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                TextView tvTime = findViewById(R.id.tvStatTime);
                tvTime.setText("시간: " + time);

                // 1초 간격으로 다시 자신을 호출
                timerHandler.postDelayed(this, 1000);
            }
        };

        // ② 타이머 시작 시각 기록 및 런처블 실행
        startTimeMillis = SystemClock.elapsedRealtime();
        timerHandler.post(timerRunnable);

        // 1) End Run 버튼 찾기
        btnEndRun = findViewById(R.id.btnEndRun);

        // 2) 클릭 시 타이머 중단 & FeedbackActivity로 이동
        btnEndRun.setOnClickListener(v -> {
            // a) 타이머 멈추기
            timerHandler.removeCallbacks(timerRunnable);

            // b) 피드백 화면으로 이동
            Intent i = new Intent(this, FeedbackActivity.class);
            // 원한다면 run 데이터(시간/거리 등)를 extras로 전달
            i.putExtra("elapsedTime", SystemClock.elapsedRealtime() - startTimeMillis);
            startActivity(i);

            // c) 현재 화면 종료
            finish();
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // res/menu/run_menu.xml 을 인플레이트
        getMenuInflater().inflate(R.menu.run_menu, menu);
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
