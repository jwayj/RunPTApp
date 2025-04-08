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

public class RunningFragment extends Fragment {
    private WebView webView;
    private Button startButton;
    private static final int ROUTE_REQUEST = 100;

    private LocalWebServer localWebServer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_running, container, false);

        webView = view.findViewById(R.id.webview);

        // 로컬 웹 서버 시작
        startLocalWebServer();

        // 웹뷰 설정
        setupWebView();
        setupButton(view); // 뷰를 전달

        return view;
    }


    private void startLocalWebServer() {
        try {
            localWebServer = new LocalWebServer(4567); // 포트 번호 4567로 설정
            localWebServer.start();
            Log.d("LocalWebServer", "Local web server started at http://localhost:4567/");
        } catch (IOException e) {
            Log.e("LocalWebServer", "Failed to start local web server: " + e.getMessage());
        }
    }

    private void setupWebView() {
        // WebViewAssetLoader 설정
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(requireContext()))
                .addPathHandler("/files/", new WebViewAssetLoader.InternalStoragePathHandler(requireContext(),
                        requireContext().getFilesDir()))
                .build(); // setDomain 제거

        // WebViewClient 설정
        webView.setWebViewClient(new LocalContentWebViewClient(assetLoader));

        // WebChromeClient 설정 - 팝업 지원 및 JavaScript 다이얼로그 처리
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                // 팝업 창 처리
                WebView newWebView = new WebView(requireContext());
                WebSettings newSettings = newWebView.getSettings();
                newSettings.setJavaScriptEnabled(true);
                newSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                newSettings.setSupportMultipleWindows(true);

                // AlertDialog로 팝업 표시
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setView(newWebView);
                AlertDialog dialog = builder.create();
                dialog.show();

                // WebView 연결
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                return true;
            }
        });

        // WebView 설정
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 팝업 허용
        settings.setDomStorageEnabled(true); // DOM 스토리지 활성화
        settings.setSupportMultipleWindows(true); // 다중 창 지원

        // HTML 파일 로드
        webView.loadUrl("http://localhost:4567/");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 웹 서버 종료
        if (localWebServer != null) {
            localWebServer.stop();
            Log.d("LocalWebServer", "Local web server stopped.");
        }
    }

    // 로컬 콘텐츠 처리를 위한 WebViewClient 구현
    private static class LocalContentWebViewClient extends WebViewClient {
        private final WebViewAssetLoader mAssetLoader;

        LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            mAssetLoader = assetLoader;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return mAssetLoader.shouldInterceptRequest(request.getUrl());
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // 오류 로깅 추가
            Log.e("WebView", "Error loading URL: " + failingUrl + " - " + description);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ROUTE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            String geoJson = data.getStringExtra("route_data");
            displayRouteOnMap(geoJson);
        }
    }

    private void displayRouteOnMap(String geoJson) {
        // JavaScript로 GeoJSON 데이터 전달
        if (geoJson != null) {
            webView.evaluateJavascript(
                    "javascript:displayRoute(" + geoJson + ")", null
            );
        }
    }
}
