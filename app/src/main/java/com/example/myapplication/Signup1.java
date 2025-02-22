package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;

public class Signup1 extends AppCompatActivity {

    private CheckBox checkAll, checkTerms, checkPrivacyPolicy, checkLocationService;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup1);

        // Initialize views
        checkAll = findViewById(R.id.check_all);
        checkTerms = findViewById(R.id.check_terms);
        checkPrivacyPolicy = findViewById(R.id.check_privacy_policy);
        checkLocationService = findViewById(R.id.check_location_service);
        confirmButton = findViewById(R.id.confirm_button);

        // Set up "Select All" checkbox behavior
        checkAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkTerms.setChecked(isChecked);
            checkPrivacyPolicy.setChecked(isChecked);
            checkLocationService.setChecked(isChecked);
        });

        // Set up individual checkbox behavior to update "Select All" state
        View.OnClickListener individualCheckboxListener = v -> {
            boolean allChecked = checkTerms.isChecked() &&
                    checkPrivacyPolicy.isChecked() &&
                    checkLocationService.isChecked();
            checkAll.setChecked(allChecked);
        };

        checkTerms.setOnClickListener(individualCheckboxListener);
        checkPrivacyPolicy.setOnClickListener(individualCheckboxListener);
        checkLocationService.setOnClickListener(individualCheckboxListener);

        // Confirm button click listener
        confirmButton.setOnClickListener(v -> {
            if (checkTerms.isChecked() &&
                    checkPrivacyPolicy.isChecked() &&
                    checkLocationService.isChecked()) {
                // Create an Intent to start Signup2 activity
                Intent intent = new Intent(Signup1.this, Signup2.class);
                startActivity(intent);
            } else {
                Toast.makeText(Signup1.this, "모든 약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}