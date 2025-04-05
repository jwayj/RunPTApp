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


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;


import com.example.myapplication.utils.Converter;
import com.example.myapplication.data.PaginationController;
import com.example.myapplication.R;
import com.example.myapplication.data.RecordData;
import com.example.myapplication.data.RecordDetail;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RecordFragment extends Fragment {
    private FirebaseFirestore db;
    private CollectionReference recordsRef;

    private int totalPages = 1; // 총 페이지 수
    private final int pageSize = 3; // 한 페이지에 표시할 레코드 수
    private int currentPage = 1; // 현재 페이지
    private List<RecordData> records = new ArrayList<>(); // 레코드 리스트

    private LinearLayout buttonContainer; // 버튼 컨테이너 선언

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mView = null; // 뷰 참조 초기화
    }

    private View mView;
    private Context mContext;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        mContext = requireContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordtotal, container, false);

        // Firebase Cloud Firestore 인스턴스 생성
        db = FirebaseFirestore.getInstance();
        recordsRef = db.collection("records");

        // 데이터 로딩 및 UI 업데이트
        loadData();

        return view;
    }

    private void loadData() {
        recordsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<RecordData> records = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    RecordData record = document.toObject(RecordData.class);
                    records.add(record);
                }

                // 페이지네이션 및 UI 업데이트
                totalPages = (records.size() + pageSize - 1) / pageSize;
                if (totalPages < 1) totalPages = 1;

                requireActivity().runOnUiThread(() -> {
                    displayPage(currentPage);
                    setupPagination();
                });
            } else {
                Log.d("Firebase", "Error getting documents: ", task.getException());
            }
        });
    }

    private void displayPage(int page) {
        buttonContainer.removeAllViews();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, records.size());
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        // 날짜 포맷: "yyyy년 MM월 dd일 EEE"
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 EEE", Locale.getDefault());

        for (int i = startIndex; i < endIndex; i++) {
            final RecordData record = records.get(i);
            // custom_button.xml을 인플레이트하여 버튼 생성
            View customButton = inflater.inflate(R.layout.custom_button, buttonContainer, false);

            // custom_button.xml 내부의 TextView 참조
            TextView tvDate = customButton.findViewById(R.id.tvDate);
            TextView tvTime = customButton.findViewById(R.id.tvTime);
            TextView tvDistance = customButton.findViewById(R.id.tvDistance);
            TextView tvPace = customButton.findViewById(R.id.tvPace);

            // 데이터 포맷팅
            String formattedDate = dateFormat.format(new Date(record.getInsertionDate()));
            String formattedTime = Converter.secondsToHMS(record.getTime());
            String formattedDistance = record.getDistance() + " km";
            String formattedPace = Converter.calculatePace(record.getTime(), record.getDistance());

            // 각 TextView에 데이터 설정
            tvDate.setText(formattedDate);
            tvTime.setText(formattedTime);
            tvDistance.setText(formattedDistance);
            tvPace.setText(formattedPace);

            // 커스텀 버튼 클릭 시 RecordDetail로 이동
            customButton.setOnClickListener(v -> {
                Log.d("CustomButton", "Clicked record id: " + record.getId());
                Intent intent = new Intent(requireContext(), RecordDetail.class);
                intent.putExtra("record_id", record.getId());
                startActivity(intent);
            });

            // 버튼 컨테이너에 추가
            buttonContainer.addView(customButton);
        }
    }

    private void saveRecord(RecordData record) {
        recordsRef.add(record).addOnSuccessListener(documentReference -> {
                    Log.d("Firebase", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Firebase", "Error adding document", e);
                });
    }
    private void setupPagination() {
        PaginationController paginationController = new PaginationController(
                requireContext(),
                (LinearLayout) requireView().findViewById(R.id.paginationContainer), // 페이지네이션 컨테이너
                currentPage,
                totalPages,
                5, // 한 그룹당 페이지 수
                page -> {
                    currentPage = page;
                    displayPage(currentPage);
                }
        );
        paginationController.render();
    }


}
