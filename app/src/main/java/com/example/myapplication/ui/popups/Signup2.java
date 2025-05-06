package com.example.myapplication.ui.popups;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Signup2 extends AppCompatActivity {
    private TextView back;
    private EditText name, id, pw, pw2, email, phone;
    private Button pwcheck, submit, idcheck;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // 뷰 연결
        back    = findViewById(R.id.back_button);
        name    = findViewById(R.id.signName);
        id      = findViewById(R.id.signID);
        pw      = findViewById(R.id.signPW);
        pw2     = findViewById(R.id.signPW2);
        email   = findViewById(R.id.signmail);
        phone   = findViewById(R.id.signphone);
        idcheck = findViewById(R.id.idcheckbutton);
        pwcheck = findViewById(R.id.pwcheckbutton);
        submit  = findViewById(R.id.signupbutton);

        // 뒤로가기
        back.setOnClickListener(v -> onBackPressed());

        // 아이디 중복 확인 (username 필드로 쿼리)
        idcheck.setOnClickListener(v -> {
            String userIdInput = id.getText().toString().trim();
            if (userIdInput.isEmpty()) {
                Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users")
                    .whereEqualTo("username", userIdInput)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this,
                                    "중복 확인 실패: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        QuerySnapshot snap = task.getResult();
                        if (snap != null && !snap.isEmpty()) {
                            Toast.makeText(this,
                                    "이미 사용 중인 아이디입니다.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "사용 가능한 아이디입니다.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 비밀번호 일치 확인
        pwcheck.setOnClickListener(v -> {
            if (pw.getText().toString()
                    .equals(pw2.getText().toString())) {
                Toast.makeText(this,
                        "비밀번호가 일치합니다.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "비밀번호가 다릅니다.",
                        Toast.LENGTH_LONG).show();
            }
        });

        // 회원가입 처리
        submit.setOnClickListener(v -> {
            Toast.makeText(this, "버튼 클릭 감지!", Toast.LENGTH_SHORT).show();
            String userIdInput   = id.getText().toString().trim();
            String nameInput     = name.getText().toString().trim();
            String emailInput    = email.getText().toString().trim();
            String passwordInput = pw.getText().toString().trim();
            String phoneInput    = phone.getText().toString().trim();

            // 입력 검증
            if (userIdInput.isEmpty()
                    || nameInput.isEmpty()
                    || emailInput.isEmpty()
                    || passwordInput.isEmpty()
                    || phoneInput.isEmpty()) {
                Toast.makeText(this,
                        "모든 항목을 입력해주세요.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passwordInput.equals(
                    pw2.getText().toString().trim())) {
                Toast.makeText(this,
                        "비밀번호가 일치하지 않습니다.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Auth로 사용자 생성
            mAuth.createUserWithEmailAndPassword(
                            emailInput, passwordInput)
                    .addOnCompleteListener(this, task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this,
                                    "회원가입 실패: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 생성된 사용자 UID를 문서 ID로 사용
                        String uid = mAuth.getCurrentUser().getUid();

                        // 저장할 데이터 준비
                        Map<String,Object> userData = new HashMap<>();
                        userData.put("username", userIdInput);
                        userData.put("name",     nameInput);
                        userData.put("email",    emailInput);
                        userData.put("phone",    phoneInput);

                        // Firestore 쓰기
                        db.collection("users").document(uid)
                                .set(userData)
                                .addOnCompleteListener(dbTask -> {
                                    Log.d("Signup2","Firestore onComplete, success="+dbTask.isSuccessful());
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this,"Firestore 쓰기 성공",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, Signup3.class));
                                        finish();
                                    } else {
                                        Log.e("Signup2","Firestore 실패", dbTask.getException());
                                        Toast.makeText(this,"Firestore 실패: "+dbTask.getException().getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                });
                    });
        });
    }
}
