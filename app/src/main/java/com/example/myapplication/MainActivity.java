package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.util.shapes.GHPoint;


import java.util.Locale; // Locale 클래스는 Java 표준 라이브러리에서 제공

public class MainActivity extends FragmentActivity {

    private ProgressBar progressBar; // 로딩화면을 위한 ProgressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar); // ProgressBar 초기화

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
                // GraphHopper 인스턴스 생성
                GraphHopper hopper = createGraphHopperInstance();

                // 시작점과 도착점의 좌표를 설정합니다.
                // 예: 서울 시청
                double startLat = 37.566535;
                double startLon = 126.977969;
                double endLat = 37.551254;
                double endLon = 126.988224;

                // 경로 생성
                ResponsePath path = routing(hopper, new GHPoint(startLat, startLon), new GHPoint(endLat, endLon));

                if (path != null) {
                    System.out.println("경로 거리: " + path.getDistance() + " 미터");
                    // 경로 데이터를 처리합니다.
                    // 예: 경로를 화면에 표시하거나 저장합니다.
                }

                // GraphHopper 인스턴스 종료
                hopper.close();

                runOnUiThread(() -> {
                    displayRoute(); // 경로 생성 완료 후 호출
                });
            } catch (Exception e) {
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

    private GraphHopper createGraphHopperInstance() {
        // GraphHopper 인스턴스 생성 및 설정
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(getExternalFilesDir(null) + "/south-korea-latest.osm.pbf");
        hopper.setGraphHopperLocation(getExternalFilesDir(null) + "/routing-graph-cache");

        // 필요한 모든 Encoded Values 추가
        hopper.setEncodedValuesString("foot_access, foot_average_speed, road_class, max_speed");

        // CustomModel 설정
        // ...

        // Profile 설정
        Profile footProfile = new Profile("foot")
                .setWeighting("custom");
        hopper.setProfiles(footProfile);

        // CH 및 LM 설정
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("foot"));
        hopper.getLMPreparationHandler().setLMProfiles(new LMProfile("foot"));

        hopper.importOrLoad();
        return hopper;
    }

    private ResponsePath routing(GraphHopper hopper, GHPoint start, GHPoint end) {
        GHRequest req = new GHRequest(start, end)
                .setProfile("foot")  // 보행자 프로필 사용
                .setLocale(Locale.US);

        GHResponse rsp = hopper.route(req);

        if (rsp.hasErrors()) {
            throw new RuntimeException(rsp.getErrors().toString());
        }

        ResponsePath path = rsp.getBest();

        // 경로 정보 출력
        System.out.println("총 거리: " + path.getDistance() + " 미터");
        System.out.println("예상 소요 시간: " + (path.getTime() / 60000) + " 분");

        return path;
    }
}


