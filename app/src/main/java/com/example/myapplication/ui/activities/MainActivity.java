package com.example.myapplication.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragments.BadgeFragment;
import com.example.myapplication.ui.fragments.MyPageFragment;
import com.example.myapplication.ui.fragments.RecordFragment;
import com.example.myapplication.ui.fragments.RunningFragment;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 기본 Fragment를 RunningFragment로 설정
        if (savedInstanceState == null) { // Activity가 처음 생성된 경우에만 실행
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RunningFragment())
                    .commit();
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        ImageView challengeIcon = findViewById(R.id.ic_challenge_icon);
        ImageView runningIcon = findViewById(R.id.nav_running_icon);
        ImageView recordIcon = findViewById(R.id.nav_record_icon);
        ImageView mypageIcon = findViewById(R.id.nav_mypage_icon);

        View.OnClickListener navigationClickListener = v -> {
            Fragment selectedFragment = null;
            int id = v.getId();

            if (id == R.id.ic_challenge_icon) {
                selectedFragment = new BadgeFragment();
            } else if (id == R.id.nav_running_icon) {
                selectedFragment = new RunningFragment(); // RunningFragment로 이동
            } else if (id == R.id.nav_record_icon) {
                selectedFragment = new RecordFragment();
            } else if (id == R.id.nav_mypage_icon) {
                selectedFragment = new MyPageFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
        };

        challengeIcon.setOnClickListener(navigationClickListener);
        runningIcon.setOnClickListener(navigationClickListener);
        recordIcon.setOnClickListener(navigationClickListener);
        mypageIcon.setOnClickListener(navigationClickListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 다시 RunningFragment 보여질 때
        Fragment frag = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (frag instanceof RunningFragment) {
            ((RunningFragment) frag).resetToStart();
        }
    }
}




