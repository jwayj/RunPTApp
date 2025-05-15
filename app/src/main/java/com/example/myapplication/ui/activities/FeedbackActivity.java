package com.example.myapplication.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedbackActivity extends AppCompatActivity {

    private WebView webviewFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // ── ① WebView 바인딩 및 설정 ──
        webviewFeedback = findViewById(R.id.webviewFeedback);
        WebSettings ws = webviewFeedback.getSettings();
        ws.setUseWideViewPort(true);
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);

        webviewFeedback.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void returnToMain() {
                runOnUiThread(() -> {
                    Intent intent = new Intent(FeedbackActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }
        }, "AndroidNative");

        webviewFeedback.setWebViewClient(new WebViewClient());
        webviewFeedback.setWebChromeClient(new WebChromeClient());
        webviewFeedback.loadUrl("https://f7c3-222-110-177-88.ngrok-free.app/map.html");

        // ── ② 날짜 표시 ──
        TextView tvDate = findViewById(R.id.tvDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.KOREA);
        String today = sdf.format(new Date());
        tvDate.setText(today);

        // ── ③ 경과 시간 표시 ──
        TextView tvTime = findViewById(R.id.tvTime);
        long elapsed = getIntent().getLongExtra("elapsedTime", 0);
        String timeStr = formatElapsed(elapsed);
        tvTime.setText(timeStr);

        // ── ④ 거리 표시 ──
        TextView tvDistance = findViewById(R.id.tvDistance);
        float distance = getIntent().getFloatExtra("distance", 0f);
        tvDistance.setText(String.format(Locale.KOREA, "%.2f", distance));

        // ── ⑤ 평균 페이스 표시 ──
        TextView tvPace = findViewById(R.id.tvPace);
        if (distance > 0.01f) {
            float elapsedMin = elapsed / 60000f;
            float pace = elapsedMin / distance;
            tvPace.setText(String.format(Locale.KOREA, "%.2f 분/km", pace));
        } else {
            tvPace.setText("평균 페이스: 계산 불가");
        }
        // ── 6. 평균 페이스 표시 ──
        double elevationGain = getIntent().getDoubleExtra("elevationGain", 0.0);
        TextView tvElevation = findViewById(R.id.tvElevation);
        tvElevation.setText(String.format(Locale.KOREA, "%.1f m", elevationGain));

    }

    private String formatElapsed(long millis) {
        int h = (int)(millis / 3600000);
        int m = (int)((millis % 3600000) / 60000);
        int s = (int)((millis % 60000) / 1000);
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
