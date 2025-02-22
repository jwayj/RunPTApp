package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.room.Room;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RecordDetail extends AppCompatActivity {

    private AppDatabase db;
    private TextView tvTime, tvDistance, tvPace, tvDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recorddetail);

        tvTime = findViewById(R.id.tvTime);
        tvDistance = findViewById(R.id.tvDistance);
        tvPace = findViewById(R.id.tvPace);
        tvDate = findViewById(R.id.tvDate);

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "my_database.db")
                .fallbackToDestructiveMigration()
                .build();

        // MainActivity에서 전달받은 record id를 가져옴
        final int recordId = getIntent().getIntExtra("record_id", -1);
        Log.d("DB_LOG", "Received record id: " + recordId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                RecordData record = db.RecordDataDao().getRecordById(recordId);
                if (record != null) {
                    // 날짜 포맷 예제 (요일도 포함: "yyyy-MM-dd EEE")
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 EEE", Locale.getDefault());
                    final String formattedDate = record.insertionDate != null ? sdf.format(record.insertionDate) : "N/A";

                    String formattedTime=Converter.secondsToHMS(record.time);
                    String formattedPace=Converter.calculatePace(record.time,record.distance);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTime.setText(String.valueOf(formattedTime));
                            tvDistance.setText(String.valueOf(record.distance));
                            tvPace.setText(String.valueOf(formattedPace));
                            tvDate.setText(formattedDate);
                        }
                    });
                } else {
                    Log.d("DB_LOG", "Record not found for id: " + recordId);
                }
            }
        }).start();



        // ScrollView에 시스템 인셋(예: 노치, 상태바 등) 적용하기
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.image_background), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        // 버튼 객체 참조
        ImageButton buttonBack = findViewById(R.id.button_back);

        // 클릭 리스너 설정
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // SecondActivity로 이동하는 Intent 생성 및 실행
                Intent intent = new Intent(RecordDetail.this, RecordFragment.class);
                startActivity(intent);
            }
        });
    }
}