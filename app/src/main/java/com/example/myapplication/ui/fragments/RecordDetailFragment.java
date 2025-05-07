package com.example.myapplication.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.utils.Converter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordDetailFragment extends Fragment {
    public static final String ARG_RECORD_ID = "record_id";

    private TextView tvDetailDate;
    private TextView tvDetailDistance;
    private TextView tvDetailTime;
    private TextView tvDetailPace;
    private TextView tvDetailElevation;

    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);  // 프래그먼트 메뉴 활성화
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // fragment_recorddetail.xml 레이아웃 inflate
        View view = inflater.inflate(R.layout.fragment_recorddetail, container, false);
        // 뷰 바인딩
        tvDetailDate      = view.findViewById(R.id.tvDate);
        tvDetailDistance  = view.findViewById(R.id.tvDistance);
        tvDetailTime      = view.findViewById(R.id.tvTime);
        tvDetailPace      = view.findViewById(R.id.tvPace);
        tvDetailElevation = view.findViewById(R.id.tvElevation);

        db = FirebaseFirestore.getInstance();

        // 인자로 전달된 record_id 가져오기
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_RECORD_ID)) {
            String recordId = args.getString(ARG_RECORD_ID);
            loadDetail(recordId);
        }
        return view;
    }

    private void loadDetail(String recordId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .collection("runs")
                .document(recordId)
                .get()
                .addOnSuccessListener(this::populateDetail)
                .addOnFailureListener(e -> {
                    // 실패 시 로그 혹은 토스트 처리
                    e.printStackTrace();
                });
    }

    private void populateDetail(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return;

        // 서버 타임스탬프
        long ts = doc.getTimestamp("timestamp").toDate().getTime();
        // 거리(km)
        double dist = doc.getDouble("distanceKm");
        // 소요시간(ms)
        long duration = doc.getLong("durationMillis");
        long timesec=duration/1000;
        // 페이스(min/km)
        double pace = doc.getDouble("paceMinPerKm");
        // 고도(m)
        double elev = doc.getDouble("elevationGain");

        // 포맷
        String dateStr = new SimpleDateFormat(
                "yyyy년 MM월 dd일 EEE", Locale.getDefault()
        ).format(new Date(ts));
        String timeStr = Converter.millisToHMS(duration);
        String distStr = String.format(Locale.getDefault(), "%.2f ", dist);
        String paceStr = Converter.calculatePace(timesec,dist);
        String elevStr = String.format(Locale.getDefault(), "%.0f m", elev);

        // UI에 반영
        tvDetailDate.setText(dateStr);
        tvDetailDistance.setText(distStr);
        tvDetailTime.setText(timeStr);
        tvDetailPace.setText(paceStr);
        tvDetailElevation.setText(elevStr);
    }

}

