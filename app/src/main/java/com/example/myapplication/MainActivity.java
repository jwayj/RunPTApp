package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 항상 RunningFragment를 표시
        if (savedInstanceState == null) {
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
                //selectedFragment = new BadgeFragment();
            } else if (id == R.id.nav_running_icon) {
                selectedFragment = new RunningFragment();
            } else if (id == R.id.nav_record_icon) {
                //selectedFragment = new RecordFragment();
            } else if (id == R.id.nav_mypage_icon) {
                //selectedFragment = new MyPageFragment();
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
}
