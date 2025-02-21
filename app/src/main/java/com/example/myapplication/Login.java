package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;

public class Login extends AppCompatActivity {
    TextView sign;
    AppCompatButton loginButton; // 로그인 버튼 추가
    EditText editID, editPassword; // 아이디와 비밀번호 입력 필드 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 아이디와 비밀번호 입력 필드 초기화
        editID = findViewById(R.id.editID);
        editPassword = findViewById(R.id.editPassword);

        // 로그인 버튼
        loginButton = findViewById(R.id.loginbutton);
        loginButton.setOnClickListener(v -> {
            System.out.println("login");
            // 로그인 버튼 클릭 시 MainActivity로 이동
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });


//        // 회원가입 버튼
//        sign = findViewById(R.id.signupbutton);
//        sign.setOnClickListener(v -> {
//            Intent intent = new Intent(this, Signup1.class);
//            startActivity(intent);
//        });
    }
}