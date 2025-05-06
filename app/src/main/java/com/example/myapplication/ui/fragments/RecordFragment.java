package com.example.myapplication.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.data.PaginationController;
import com.example.myapplication.data.RecordData;
import com.example.myapplication.ui.fragments.RecordDetailFragment;
import com.example.myapplication.utils.Converter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordFragment extends Fragment {
    private FirebaseFirestore db;
    private LinearLayout buttonContainer;
    private LinearLayout paginationContainer;

    private static final int PAGE_SIZE = 3;
    private int currentPage = 1;
    private int totalPages = 1;
    private final List<RecordData> records = new ArrayList<>();

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordtotal, container, false);
        mContext = requireContext();

        // 뷰 초기화
        buttonContainer = view.findViewById(R.id.buttonContainer);
        paginationContainer = view.findViewById(R.id.paginationContainer);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 데이터 로드
        loadData();
        return view;
    }

    private void loadData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .collection("runs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("RecordFragment", "Error loading runs", task.getException());
                        return;
                    }
                    QuerySnapshot qs = task.getResult();
                    records.clear();
                    if (qs != null) {
                        for (DocumentSnapshot doc : qs.getDocuments()) {
                            // Firestore 필드 읽기
                            long insertionDate = 0;
                            if (doc.getTimestamp("timestamp") != null) {
                                insertionDate = doc.getTimestamp("timestamp").toDate().getTime();
                            }
                            int timeSec = 0;
                            if (doc.contains("durationMillis")) {
                                // 저장 시 밀리초 단위로 저장했다면 초 단위 변환 필요
                                long durMs = doc.getLong("durationMillis");
                                timeSec = (int) (durMs / 1000);
                            }
                            double distanceKm = 0;
                            if (doc.contains("distanceKm")) {
                                distanceKm = doc.getDouble("distanceKm");
                            }

                            // RecordData 생성 및 추가
                            RecordData data = new RecordData(
                                    doc.getId(),
                                    insertionDate,
                                    timeSec,
                                    distanceKm
                            );
                            records.add(data);
                        }
                    }

                    totalPages = Math.max(1, (records.size() + PAGE_SIZE - 1) / PAGE_SIZE);
                    requireActivity().runOnUiThread(() -> {
                        displayPage(currentPage);
                        setupPagination();
                    });
                });
    }

    private void displayPage(int page) {
        buttonContainer.removeAllViews();
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, records.size());
        LayoutInflater inflater = LayoutInflater.from(mContext);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy년 MM월 dd일 EEE", Locale.getDefault());

        for (int i = start; i < end; i++) {
            RecordData rec = records.get(i);
            View item = inflater.inflate(R.layout.custom_button, buttonContainer, false);

            TextView tvDate     = item.findViewById(R.id.tvDate);
            TextView tvTime     = item.findViewById(R.id.tvTime);
            TextView tvDistance = item.findViewById(R.id.tvDistance);
            TextView tvPace     = item.findViewById(R.id.tvPace);

            // 데이터 포맷팅
            String dateStr = dateFormat.format(new Date(rec.getInsertionDate()));
            String timeStr = Converter.secondsToHMS(rec.getTime());
            String distStr = String.format(Locale.getDefault(), "%.2f km", rec.getDistance());
            String paceStr = Converter.calculatePace(rec.getTime(), rec.getDistance());

            tvDate.setText(dateStr);
            tvTime.setText(timeStr);
            tvDistance.setText(distStr);
            tvPace.setText(paceStr);

            // 상세 프래그먼트로 전환
            item.setOnClickListener(v -> {
                RecordDetailFragment detailFrag = new RecordDetailFragment();
                Bundle args = new Bundle();
                args.putString("record_id", rec.getId());
                detailFrag.setArguments(args);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFrag)
                        .addToBackStack(null)
                        .commit();
            });

            buttonContainer.addView(item);
        }
    }

    private void setupPagination() {
        PaginationController controller = new PaginationController(
                mContext,
                paginationContainer,
                currentPage,
                totalPages,
                5,
                page -> {
                    currentPage = page;
                    displayPage(currentPage);
                }
        );
        controller.render();
    }
}
