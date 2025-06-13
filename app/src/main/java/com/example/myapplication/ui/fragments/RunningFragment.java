package com.example.myapplication.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.ui.activities.RunActivity;

import android.webkit.JavascriptInterface;
import android.content.Intent;

import android.app.AlertDialog;
import android.webkit.JsResult;
import android.webkit.GeolocationPermissions;


public class RunningFragment extends Fragment {
    private WebView webView;
    private Button startButton;
    private RelativeLayout subBaseView;
    private Button btnLoadMap;
    private int counter = 10;

    private String geoJsonId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_running, container, false);

        btnLoadMap  = view.findViewById(R.id.btnLoadMap);
        subBaseView = view.findViewById(R.id.sub_webview);
        webView = view.findViewById(R.id.webview);

        // 2) 버튼 클릭 시
        btnLoadMap.setOnClickListener(v -> {
            btnLoadMap.setVisibility(View.GONE);   // 버튼 숨기고
            webView.setVisibility(View.VISIBLE);   // WebView 보이기
            setupWebView();                        // WebView 로드 시작
        });

        setupBackPressHandler();
        return view;
    }


    private void setupBackPressHandler() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (subBaseView.getChildCount() > 0) {
                    WebView lastWebView = (WebView) subBaseView.getChildAt(subBaseView.getChildCount() - 1);
                    if (lastWebView.canGoBack()) {
                        lastWebView.goBack();
                    } else {
                        subBaseView.removeView(lastWebView);
                        counter -= 10;
                        if (subBaseView.getChildCount() == 0) {
                            subBaseView.setVisibility(View.GONE);
                            counter = 10;
                            webView.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    public class HelloWebChromeClient extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean userGesture, Message resultMsg) {
            subBaseView.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);

            // HelloWebChromeClient 클래스 내부 수정
            WebView newWebView = new WebView(getContext());
            WebSettings settings = newWebView.getSettings();

// 필수 JavaScript 설정
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);  // 데이터베이스 지원 추가 [2][6]
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);

// 멀티 윈도우 지원
            settings.setSupportMultipleWindows(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);

// 보안 설정
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);  // HTTP/HTTPS 혼용 허용 [4]

// 인코딩 설정
            settings.setDefaultTextEncodingName("utf-8");


            newWebView.setWebChromeClient(this);
            newWebView.setWebViewClient(new WebViewClient());

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            params.setMargins(counter, counter, counter, counter);
            newWebView.setLayoutParams(params);

            subBaseView.addView(newWebView);
            counter += 10;

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();

            return true;
        }
        // 호출하면 처음 상태로 리셋
        @Override
        public void onCloseWindow(WebView window) {
            subBaseView.removeView(window);
            counter -= 10;
            if (subBaseView.getChildCount() == 0) {
                subBaseView.setVisibility(View.GONE);
                counter = 10;
                webView.setVisibility(View.VISIBLE);
            }
        }

    }
    public void resetToStart() {
        btnLoadMap.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        subBaseView.setVisibility(View.GONE);
    }
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        // WebView 설정
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true); // 파일 접근 허용
        webView.getSettings().setAllowContentAccess(true); // 콘텐츠 접근 허용
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setDatabaseEnabled(true);    // 데이터베이스 지원 추가
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setGeolocationEnabled(true);

        settings.setGeolocationDatabasePath(getContext().getFilesDir().getPath());

        // ← 이 라인 추가: HTML의 AndroidBridge.setGeoJsonId(...) 호출을 받습니다.
        webView.addJavascriptInterface(new JSBridge(), "AndroidBridge");

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void startNativeRun() {
                // UI 스레드에서 RunActivity 실행
                requireActivity().runOnUiThread(() -> {
                    Intent intent = new Intent(getContext(), RunActivity.class);
                    intent.putExtra("GEOJSON_ID", geoJsonId);
                    startActivity(intent);
                });
            }
        }, "AndroidNative");

        // Android 5.0 이상에서 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }


        webView.setWebChromeClient(new CustomWebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm())
                        .show();
                return true; // 직접 처리했음을 알림
            }
        });


        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://15.164.222.69:80");

    }

    /** HTML → Android 브릿지 **/
    private class JSBridge {
        /** meta.json 에서 읽어온 geoJsonId 를 여기로 전달받습니다 */
        @JavascriptInterface
        public void setGeoJsonId(String id) {
            geoJsonId = id;
            Log.d("RunningFragment", "JSBridge.setGeoJsonId() 호출, id = " + geoJsonId);
        }

        /** “러닝 시작” 버튼 클릭 시 호출되어 RunActivity 로 넘깁니다 */
        @JavascriptInterface
        public void onStartRun() {
            requireActivity().runOnUiThread(() -> {
                Intent intent = new Intent(getContext(), RunActivity.class);
                intent.putExtra("GEOJSON_ID", geoJsonId);
                startActivity(intent);
            });
        }
    }
    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            // 위치 권한 요청 다이얼로그 표시
            new AlertDialog.Builder(getContext())
                    .setTitle("위치 정보 요청")
                    .setMessage(origin + "에서 위치 정보를 사용하려고 합니다.")
                    .setPositiveButton("허용", (dialog, which) -> {
                        callback.invoke(origin, true, false);  // 권한 허용
                    })
                    .setNegativeButton("거부", (dialog, which) -> {
                        callback.invoke(origin, false, false); // 권한 거부
                    })
                    .show();
        }
    }
}