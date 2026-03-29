package com.saveetha.tricholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DiagnoseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        // Find Diagnose button
        Button diagnoseButton = findViewById(R.id.diagnoseButton);

        // Diagnose -> Go to ObjectDetectedActivity
        diagnoseButton.setOnClickListener(v -> {
            android.widget.Toast
                    .makeText(this, "WARNING: DUMMY DIAGNOSE ACTIVITY (Wrong Path)", android.widget.Toast.LENGTH_LONG)
                    .show();
            // Intent intent = new Intent(this, ObjectDetectedActivity.class);
            // startActivity(intent);
            // finish();
        });
    }
}
