package com.example.myapplication.ui.popups;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.routing_module.RoutingCore;

import java.io.IOException;
import java.util.List;


public class Popup extends AppCompatActivity {

    private EditText editStart;
    private EditText editDestination;
    private EditText editDistance; // 거리 입력 EditText 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        // 닫기 버튼
        ImageView closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());

        // 출발지 입력창 클릭 시 검색 팝업 띄우기
        editStart = findViewById(R.id.editStart);
        editStart.setOnClickListener(v -> {
            Intent intent = new Intent(Popup.this, SearchPopup.class);
            startActivityForResult(intent, 1); // 검색 팝업 실행
            // 키보드가 뜨지 않도록 하기 위해 EditText 포커스 제거
            editStart.clearFocus();
        });

        // 도착지 입력창 클릭 시 검색 팝업 띄우기
        editDestination = findViewById(R.id.editDestination);
        editDestination.setOnClickListener(v -> {
            Intent intent = new Intent(Popup.this, SearchPopup.class);
            startActivityForResult(intent, 2); // 검색 팝업 실행
            // 키보드가 뜨지 않도록 하기 위해 EditText 포커스 제거
            editDestination.clearFocus();
        });

        // 거리 입력
        editDistance = findViewById(R.id.editDistance); // 거리 입력칸
        editDistance.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 엔터 키가 눌렸을 때, 입력된 거리 값 저장
                String distance = editDistance.getText().toString();

                // 여기서 값을 처리하는 로직을 추가할 수 있음 (예: 서버로 전송)
                // 예시: Log.d("Distance", "입력된 거리: " + distance);

                // 키보드를 닫기
                hideKeyboard(v);
                return true;
            }
            return false;
        });
    }

    // 키보드 숨기기
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void onSearchRouteClicked(View view) {
        try {
            // 입력값 검증
            if (editStart.getText().toString().isEmpty() ||
                    editDestination.getText().toString().isEmpty() ||
                    editDistance.getText().toString().isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 좌표 파싱 (실제 구현시 Geocoding 필요)
            double startLat = Double.parseDouble(editStart.getTag().toString());
            double startLon = Double.parseDouble(editStart.getTag().toString());
            double endLat = Double.parseDouble(editDestination.getTag().toString());
            double endLon = Double.parseDouble(editDestination.getTag().toString());
            double distance = Double.parseDouble(editDistance.getText().toString());

            // 서비스 인텐트 생성
            Intent serviceIntent = new Intent(this, RoutingForegroundService.class);
            serviceIntent.putExtra("distance", distance);
            serviceIntent.putExtra("startLat", startLat);
            serviceIntent.putExtra("startLon", startLon);
            serviceIntent.putExtra("endLat", endLat);
            serviceIntent.putExtra("endLon", endLon);

            // Android 8.0 이상 대응
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "잘못된 좌표 형식", Toast.LENGTH_SHORT).show();
        }
    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && data != null) {
//            String selectedAddress = data.getStringExtra("selectedAddress");
//
//            // 주소를 좌표로 변환 (예: Google Geocoding API 사용)
//            Geocoder geocoder = new Geocoder(this);
//            try {
//                List<Address> addresses = geocoder.getFromLocationName(selectedAddress, 1);
//                if (!addresses.isEmpty()) {
//                    double lat = addresses.get(0).getLatitude();
//                    double lon = addresses.get(0).getLongitude();
//
//                    if (requestCode == 1) {
//                        editStart.setTag(new double[]{lat, lon});
//                    } else if (requestCode == 2) {
//                        editDestination.setTag(new double[]{lat, lon});
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    // 주소 선택 후 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selectedAddress");

            // 출발지 또는 도착지에 주소 입력하기
            if (requestCode == 1) {
                editStart.setText(selectedAddress); // 출발지 입력칸에 주소 반영
            } else if (requestCode == 2) {
                editDestination.setText(selectedAddress); // 도착지 입력칸에 주소 반영
            }
        }
    }
}