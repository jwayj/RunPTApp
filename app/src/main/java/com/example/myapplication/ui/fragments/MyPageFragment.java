package com.example.myapplication.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.ui.activities.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyPageFragment extends Fragment {
    private ImageView ivProfile;
    private TextView tvUsername;
    private Button btnLogout;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);

        ivProfile   = view.findViewById(R.id.ivProfile);
        tvUsername  = view.findViewById(R.id.tvUsername);
        btnLogout   = view.findViewById(R.id.btnLogout);
        mAuth       = FirebaseAuth.getInstance();

        setupProfile();
        setupLogoutButton();

        return view;
    }

    private void setupProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        // 로그인 유무 상관없이 지정된 기본 이미지 사용
        ivProfile.setImageResource(R.drawable.badgeex);

        if (user != null) {
            // 이름 또는 이메일 표시
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            tvUsername.setText(name);
        } else {
            // 미로그인 상태
            tvUsername.setText("관리자모드");
        }
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            // Firebase 로그아웃
            mAuth.signOut();
            // 로그인 화면으로 이동
            Intent it = new Intent(requireContext(), Login.class);
            startActivity(it);
            requireActivity().finish();
        });
    }
}
