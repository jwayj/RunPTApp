package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RecordDetail extends AppCompatActivity {

    private FirebaseFirestore db; // Firestore 인스턴스
    private TextView tvTime, tvDistance, tvPace, tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recorddetail);

        // View 초기화
        tvTime = findViewById(R.id.tvTime);
        tvDistance = findViewById(R.id.tvDistance);
        tvPace = findViewById(R.id.tvPace);
        tvDate = findViewById(R.id.tvDate);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // MainActivity에서 전달받은 record id를 가져옴
        final String recordId = getIntent().getStringExtra("record_id");
        Log.d("DB_LOG", "Received record id: " + recordId);

        // Firestore에서 데이터 가져오기
        if (recordId != null) {
            fetchRecord(recordId);
        } else {
            Log.d("DB_LOG", "Invalid record id");
        }

        // ScrollView에 시스템 인셋(예: 노치, 상태바 등) 적용하기
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.image_background), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        // 뒤로가기 버튼 설정
        ImageButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(view -> {
            Intent intent = new Intent(RecordDetail.this, RecordFragment.class);
            startActivity(intent);
        });
    }

    /**
     * Firestore에서 레코드 데이터를 가져오는 메서드
     *
     * @param recordId Firestore 문서 ID
     */
    private void fetchRecord(String recordId) {
        DocumentReference docRef = db.collection("records").document(recordId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                RecordData record = documentSnapshot.toObject(RecordData.class);

                if (record != null) {
                    // 날짜 포맷 예제 (요일도 포함: "yyyy-MM-dd EEE")
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 EEE", Locale.getDefault());
                    String formattedDate = sdf.format(record.getInsertionDate());

                    String formattedTime = Converter.secondsToHMS(record.getTime());
                    String formattedPace = Converter.calculatePace(record.getTime(), record.getDistance());

                    // UI 업데이트
                    tvTime.setText(formattedTime);
                    tvDistance.setText(String.valueOf(record.getDistance()));
                    tvPace.setText(formattedPace);
                    tvDate.setText(formattedDate);
                }
            } else {
                Log.d("DB_LOG", "No such document");
            }
        }).addOnFailureListener(e -> Log.d("DB_LOG", "Error fetching document", e));
    }
}
