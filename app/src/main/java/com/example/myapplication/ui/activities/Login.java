package com.example.myapplication.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatButton;

import com.example.myapplication.R;
import com.example.myapplication.ui.popups.Signup1;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    TextView sign;
    AppCompatButton loginButton;
    EditText editID, editPassword;
    private FirebaseAuth mAuth; // FirebaseAuth 인스턴스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
    }
}

