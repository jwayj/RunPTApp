package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {

    private ProgressBar progressBar; // 로딩화면을 위한 ProgressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar); // ProgressBar 초기화

        // 항상 RunningFragment를 표시
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RunningFragment())
                    .commit();
        }

        setupBottomNavigation();

        // 이전 액티비티에서 전달된 데이터를 받습니다.
        Intent intent = getIntent();
        String start = intent.getStringExtra("start");
        String destination = intent.getStringExtra("destination");
        String distance = intent.getStringExtra("distance");

        // 경로 생성을 위한 로직을 실행합니다.
        if (start != null && destination != null && distance != null) {
            createRoute(start, destination, distance);
        }
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
                selectedFragment = new RunningFragment();
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

    private void createRoute(String start, String destination, String distance) {
        // 로딩화면을 표시합니다.
        progressBar.setVisibility(View.VISIBLE);

        // 경로 생성 로직을 여기서 구현합니다.
        new Thread(() -> {
            try {
                // 예: 경로 생성 알고리즘 실행 또는 서버 요청
                Thread.sleep(3000); // 경로 생성 시뮬레이션 (3초 대기)

                runOnUiThread(() -> {
                    displayRoute(); // 경로 생성 완료 후 호출
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayRoute() {
        // 로딩화면을 숨깁니다.
        progressBar.setVisibility(View.GONE);

        // 생성된 경로를 화면에 표시합니다.
        Fragment runningFragment = new RunningFragment();

        Bundle bundle = new Bundle();
        bundle.putString("route_data", "경로 데이터"); // 예: 생성된 경로 데이터를 전달
        runningFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, runningFragment)
                .commit();
    }
}

