package com.example.myapplication.ui.popups;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.ui.activities.Login;
import com.example.myapplication.databinding.ActivitySignup3Binding;

public class Signup3 extends AppCompatActivity {
    private ActivitySignup3Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignup3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 로그인 바로가기 버튼에 대한 클릭 리스너 추가
        Button loginButton = binding.loginButton;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Signup3.this, Login.class);
                startActivity(intent);
                finish(); // 현재 액티비티를 종료합니다.
            }
        });
    }
}