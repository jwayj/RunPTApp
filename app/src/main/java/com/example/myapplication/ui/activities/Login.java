package com.example.myapplication.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatButton;

import com.example.myapplication.R;
import com.example.myapplication.ui.popups.Signup1;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    TextView sign;
    AppCompatButton loginButton;
    AppCompatButton skipButton;
    EditText editID, editPassword;
    private FirebaseAuth mAuth; // FirebaseAuth 인스턴스

    private ImageView splashBackground, splashLogo;
    private LinearLayout loginContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        splashBackground = findViewById(R.id.splashBackground);
        splashLogo = findViewById(R.id.splashLogo);
        //private LinearLayout loginContainer;
        loginContainer = findViewById(R.id.loginContainer);

        // 0.5초 후 로고 보이기
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashLogo.setVisibility(View.VISIBLE);
            splashLogo.setAlpha(0f); // 처음엔 투명
            splashLogo.animate()
                    .alpha(1f)          // 최종 불투명
                    .setDuration(1000)  // 애니메이션 지속시간: 1초
                    .start();
        }, 1000);

        // 5초 후 스플래시 모두 숨기고 로그인 UI 표시
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashBackground.setVisibility(View.GONE);
            splashLogo.setVisibility(View.GONE);
            loginContainer.setVisibility(View.VISIBLE);
        }, 4000);
        // FirebaseAuth 초기화
        mAuth = FirebaseAuth.getInstance();

        editID = findViewById(R.id.editID);
        editPassword = findViewById(R.id.editPassword);

        loginButton = findViewById(R.id.loginbutton);
        loginButton.setOnClickListener(v -> {
            String email = editID.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Authentication 로그인
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this, "로그인 실패: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        sign = findViewById(R.id.signupbutton);
        sign.setOnClickListener(v -> {
            Intent intent = new Intent(this, Signup1.class);
            startActivity(intent);
        });

        skipButton = findViewById(R.id.skipButton);
        skipButton.setOnClickListener(v ->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });


    }
}

