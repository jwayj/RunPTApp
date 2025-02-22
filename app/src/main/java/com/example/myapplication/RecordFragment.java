package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordFragment extends Fragment {

    private AppDatabase db;
    private RecordDataDao recordDataDao;
    private LinearLayout buttonContainer, paginationContainer;
    private List<RecordData> records;
    private int currentPage = 1;
    private final int pageSize = 3; // 한 페이지에 표시할 버튼(레코드) 수
    private int totalPages = 1; // 총 페이지 수
    private final int pagesPerGroup = 5; // 한 그룹에 표시할 페이지 번호 수

    private PaginationController paginationController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordtotal, container, false);

        buttonContainer = view.findViewById(R.id.buttonContainer);
        paginationContainer = view.findViewById(R.id.paginationContainer);

        // Room 데이터베이스 인스턴스 생성
        db = Room.databaseBuilder(requireContext().getApplicationContext(),
                        AppDatabase.class, "my_database.db")
                .fallbackToDestructiveMigration()
                .build();
        recordDataDao = db.RecordDataDao();

        // 데이터 로딩 및 UI 업데이트
        loadData();

        return view;
    }

    private void loadData() {
        new Thread(() -> {
            records = recordDataDao.getAllRecords();
            totalPages = (records.size() + pageSize - 1) / pageSize;
            if (totalPages < 1) totalPages = 1;

            requireActivity().runOnUiThread(() -> {
                displayPage(currentPage);
                setupPagination();
            });
        }).start();
    }

    private void setupPagination() {
        paginationController = new PaginationController(
                requireActivity(),
                paginationContainer,
                currentPage,
                totalPages,
                pagesPerGroup,
                page -> {
                    currentPage = page;
                    displayPage(currentPage);
                }
        );
        paginationController.render();
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
            String formattedDate = dateFormat.format(new Date(record.insertionDate));
            String formattedTime = Converter.secondsToHMS(record.time);
            String formattedDistance = record.distance + " km";
            String formattedPace = Converter.calculatePace(record.time, record.distance);

            // 각 TextView에 데이터 설정
            tvDate.setText(formattedDate);
            tvTime.setText(formattedTime);
            tvDistance.setText(formattedDistance);
            tvPace.setText(formattedPace);

            // 커스텀 버튼 클릭 시 RecordDetail로 이동
            customButton.setOnClickListener(v -> {
                Log.d("CustomButton", "Clicked record id: " + record.id);
                Intent intent = new Intent(requireContext(), RecordDetail.class);
                intent.putExtra("record_id", record.id);
                startActivity(intent);
            });

            // 버튼 컨테이너에 추가
            buttonContainer.addView(customButton);
        }
    }
}