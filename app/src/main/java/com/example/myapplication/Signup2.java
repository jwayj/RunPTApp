package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Signup2 extends AppCompatActivity {
    TextView back;
    EditText name, id, pw, pw2, email;
    Button pwcheck, submit;
    Button idcheck; // 중복확인 버튼 추가
    private FirebaseFirestore db; // Firestore 인스턴스 추가

    private FirebaseAuth mAuth; // FirebaseAuth 인스턴스 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);

        // FirebaseAuth 초기화
        mAuth = FirebaseAuth.getInstance();

        // 뒤로 가기 버튼
        back = findViewById(R.id.back_button);
        back.setOnClickListener(v -> onBackPressed());

        // 기입 항목
        name = findViewById(R.id.signName);
        id = findViewById(R.id.signID);
        pw = findViewById(R.id.signPW);
        pw2 = findViewById(R.id.signPW2);
        email = findViewById(R.id.signmail);

        db = FirebaseFirestore.getInstance();

        // 중복확인 버튼
        idcheck = findViewById(R.id.idcheckbutton);
        idcheck.setOnClickListener(v -> {
            String userId = id.getText().toString().trim();
            if (userId.isEmpty()) {
                Toast.makeText(Signup2.this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                Toast.makeText(Signup2.this, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Signup2.this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Exception e = task.getException();
                            if (e != null) {
                                Toast.makeText(Signup2.this, "중복 확인 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Signup2.this, "중복 확인 실패: 알 수 없는 오류", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // 비밀번호 확인 버튼
        pwcheck = findViewById(R.id.pwcheckbutton);
        pwcheck.setOnClickListener(v -> {
            if (pw.getText().toString().equals(pw2.getText().toString())) {
                Toast.makeText(Signup2.this, "비밀번호가 일치합니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Signup2.this, "비밀번호가 다릅니다.", Toast.LENGTH_LONG).show();
            }
        });

        // 회원가입 완료 버튼
        submit = findViewById(R.id.signupbutton);
        submit.setOnClickListener(v -> {
            String emailInput = email.getText().toString().trim();
            String passwordInput = pw.getText().toString().trim();

            if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(Signup2.this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pw.getText().toString().equals(pw2.getText().toString())) {
                Toast.makeText(Signup2.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(Signup2.this, Signup3.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Exception e = task.getException();
                            Toast.makeText(Signup2.this, "회원가입 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace(); // 로그캣에 자세한 오류 출력
                        }
                    });
        });
    }
}
