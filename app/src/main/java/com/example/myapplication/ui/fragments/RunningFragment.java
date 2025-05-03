package com.example.myapplication.ui.fragments;

import java.io.IOException;

import android.widget.Button;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Build;
import android.webkit.CookieManager;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebViewAssetLoader;

import com.example.myapplication.R;
import com.example.myapplication.ui.popups.Popup;
import com.example.myapplication.ui.popups.LocalWebServer;
import com.example.routing_module.RoutingCore;

import android.widget.RelativeLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;



public class RunningFragment extends Fragment {
    private WebView webView;
    private Button startButton;
    private RelativeLayout subBaseView;
    private int counter = 10;

    private SwipeRefreshLayout refreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_running, container, false);

        refreshLayout = view.findViewById(R.id.refresh_layout); // 변수 초기화
        webView = view.findViewById(R.id.webview);
        setupWebView();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload(); // 웹뷰 새로고침
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                refreshLayout.setRefreshing(false); // 새로고침 아이콘 종료
            }
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

    private void setupWebView() {
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

        // Android 5.0 이상에서 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }


        webView.setWebChromeClient(new HelloWebChromeClient());
        webView.setWebViewClient(new WebViewClient());
//        webView.loadUrl("http://10.0.2.2:4567");
        webView.loadUrl("https://cfda-110-11-97-50.ngrok-free.app/");

    }

}


