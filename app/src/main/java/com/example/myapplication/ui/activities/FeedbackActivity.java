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
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedbackActivity extends AppCompatActivity {
    private WebView webviewFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // ① WebView 바인딩 및 설정
        webviewFeedback = findViewById(R.id.webviewFeedback);
        WebSettings ws = webviewFeedback.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        // 필요 시
        // ws.setAllowFileAccess(true);
        // ws.setAllowUniversalAccessFromFileURLs(true);

        webviewFeedback.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void returnToMain() {
                runOnUiThread(() -> {
                    // 메인 액티비티로 돌아가기
                    Intent intent = new Intent(FeedbackActivity.this, MainActivity.class);
                    // 백스택 정리 옵션(원하면)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }
        }, "AndroidNative");

        webviewFeedback.setWebViewClient(new WebViewClient());
        webviewFeedback.setWebChromeClient(new WebChromeClient());
        // (b) 서버에서 내려주는 페이지를 띄울 때
        webviewFeedback.loadUrl("http://192.168.123.5:4567/map.html");

        // ② 나머지 UI 초기화 (제목, 요약, 전송 버튼 등
        TextView tvDate = findViewById(R.id.tvDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.KOREA);
        String today = sdf.format(new Date());
        tvDate.setText(today);
        TextView tvTime=findViewById(R.id.tvTime);
        long elapsed = getIntent().getLongExtra("elapsedTime", 0);
        String time = formatElapsed(elapsed);
        tvTime.setText(time);


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

