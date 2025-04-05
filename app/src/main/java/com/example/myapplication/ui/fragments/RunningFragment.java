package com.example.myapplication.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebViewAssetLoader;

import com.example.myapplication.R;
import com.example.routing_module.RoutingCore;

public class RunningFragment extends Fragment {
    private WebView webView;
    private static final int ROUTE_REQUEST = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_running, container, false);

        webView = view.findViewById(R.id.webview);
        setupWebView();

        return view;
    }

    private void setupWebView() {
        // WebView 초기화
        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(requireContext()))
                .addPathHandler("/files/", new WebViewAssetLoader.InternalStoragePathHandler(requireContext(),
                        requireContext().getFilesDir()))
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // 로컬 HTML 파일 로드
        webView.loadUrl("https://appassets.androidplatform.net/assets/map.html");
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




