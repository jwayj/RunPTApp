package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SearchPopup extends AppCompatActivity {

    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_popup);

        // 주소 입력창 찾기
        searchInput = findViewById(R.id.search_input);

        // 뒤로 가기 버튼
        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());
    }

    // 주소 목록 클릭 시 실행되는 메서드
    public void onAddressClick(View view) {
        TextView clickedAddress = (TextView) view; // 클릭된 주소 TextView
        String selectedAddress = clickedAddress.getText().toString(); // 클릭된 주소 텍스트

        // 선택된 주소를 Intent로 전달
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedAddress", selectedAddress);
        setResult(RESULT_OK, resultIntent);

        // 이전 액티비티로 돌아가기
        finish();
    }
}