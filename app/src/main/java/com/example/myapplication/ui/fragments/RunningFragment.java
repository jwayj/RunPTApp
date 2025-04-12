package com.example.myapplication.ui.fragments;

import java.io.IOException;

import android.widget.Button;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebViewAssetLoader;

import com.example.myapplication.R;
import com.example.myapplication.ui.popups.Popup;
import com.example.myapplication.ui.popups.LocalWebServer;
import com.example.routing_module.RoutingCore;

public class RunningFragment extends Fragment {
    private WebView webView;
    private Button startButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_running, container, false);

        webView = view.findViewById(R.id.webview);
        setupWebView();
        setupButton(view);

        return view;
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // Live Server URL 설정
        webView.loadUrl("http://192.168.45.173:5500/example/resources/index.html");
    }

    private void setupButton(View view) {
        startButton = view.findViewById(R.id.startButton); // 버튼 ID로 초기화
        if (startButton != null) { // NullPointerException 방지
            startButton.setOnClickListener(v -> {
                // WebView에서 데이터 가져오기
                webView.evaluateJavascript("javascript:getRouteData()", value -> {
                    // 새로운 팝업 액티비티 시작
                    Intent intent = new Intent(getActivity(), Popup.class);
                    intent.putExtra("route_data", value);
                    startActivity(intent);
                });
            });
        } else {
            Log.e("RunningFragment", "startButton not found in layout!");
        }
    }
}

