package com.example.myapplication.ui.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.ui.activities.RunActivity2;
import com.example.myapplication.utils.Converter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordDetailFragment extends Fragment {
    public static final String ARG_RECORD_ID = "record_id";

    // --- 기존 UI 요소 ---
    private TextView tvDetailDate, tvDetailDistance, tvDetailTime,
            tvDetailPace, tvDetailElevation;

    // --- WebView용 필드 ---
    private WebView mapWebView;
    private String recordId;
    private String geoJsonId;
    private String raw;
    private boolean pageLoaded   = false;
    private boolean detailLoaded = false;

    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recorddetail, container, false);

        // 1) 기존 텍스트뷰 바인딩
        tvDetailDate      = view.findViewById(R.id.tvDate);
        tvDetailDistance  = view.findViewById(R.id.tvDistance);
        tvDetailTime      = view.findViewById(R.id.tvTime);
        tvDetailPace      = view.findViewById(R.id.tvPace);
        tvDetailElevation = view.findViewById(R.id.tvElevation);

        // 2) WebView 바인딩 및 설정
        mapWebView = view.findViewById(R.id.webviewDetail);
        setupMapWebView();

        // 3) HTML 파일 로드
        mapWebView.loadUrl("http://15.164.222.69:80/mapfromfirebase.html");

        // 4) Firestore 인스턴스
        db = FirebaseFirestore.getInstance();

        // 5) recordId 받아서 상세 + geoJsonId 로드
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_RECORD_ID)) {
            recordId = args.getString(ARG_RECORD_ID);
            loadDetail(recordId);
        }

        Button btnRun = view.findViewById(R.id.btnRunThisRoute);
        btnRun.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), RunActivity2.class);
            intent.putExtra("GEOJSON_ID",geoJsonId);
            startActivity(intent);
        });

        return view;
    }

    // WebView 설정 및 JS 인터페이스 등록
    private void setupMapWebView() {
        WebSettings ws = mapWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // ① JS 인터페이스 등록 (AndroidBridge.getGeoJsonId() 사용 가능)
        mapWebView.addJavascriptInterface(new JSBridge(), "AndroidBridge");

        // ② 페이지 로드 완료 콜백
        mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageLoaded = true;
                fireIfReady();
            }
        });
        mapWebView.setWebChromeClient(new WebChromeClient());
    }

    // Firestore에서 레코드 + geoJsonId 가져오기
    private void loadDetail(String recordId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .collection("runs")
                .document(recordId)
                .get()
                .addOnSuccessListener(this::onSnapshot)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void onSnapshot(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return;

        // ① geoJsonId 필드 저장
        raw = doc.getString("geojsonID");
        // 양끝 큰따옴표 제거
        if (raw != null && raw.length() >= 2 &&
                raw.charAt(0) == '"' && raw.charAt(raw.length()-1) == '"') {
            raw = raw.substring(1, raw.length() - 1);
        }

        geoJsonId = raw;  // 이제 순수 ID만 담깁니다.
        detailLoaded = true;

        // ② 기존 상세 정보 UI 반영
        long ts       = doc.getTimestamp("timestamp").toDate().getTime();
        double dist   = doc.getDouble("distanceKm");
        long duration = doc.getLong("durationMillis");
        double pace   = doc.getDouble("paceMinPerKm");
        double elev   = doc.getDouble("elevationGain");

        String dateStr  = new SimpleDateFormat("yyyy년 MM월 dd일 EEE", Locale.getDefault())
                .format(new Date(ts));
        String timeStr  = Converter.millisToHMS(duration);
        String distStr  = String.format(Locale.getDefault(), "%.2f", dist);
        String paceStr  = Converter.calculatePace(duration/1000, pace);
        String elevStr  = String.format(Locale.getDefault(), "%.0f m", elev);

        tvDetailDate.setText(dateStr);
        tvDetailDistance.setText(distStr);
        tvDetailTime.setText(timeStr);
        tvDetailPace.setText(paceStr);
        tvDetailElevation.setText(elevStr);

        // ③ 페이지·데이터 모두 준비되면 JS 실행
        fireIfReady();
    }

    // 페이지 로드와 Firestore 로드가 모두 끝났을 때 JS 호출
    private void fireIfReady() {
        if (!pageLoaded || !detailLoaded) return;
        mapWebView.evaluateJavascript("loadMap()", null);
    }

    // JS ↔ Android 브릿지
    private class JSBridge {
        @JavascriptInterface
        public String getGeoJsonId() {
            return geoJsonId;
        }
    }
}
