package com.saveetha.tricholens;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ObjectDetectedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detected);

        findViewById(R.id.backArrow).setOnClickListener(v -> finish());

        // Initialize Views
        ImageView resultImage   = findViewById(R.id.resultImage);
        TextView  nameText      = findViewById(R.id.nameText);
        TextView  densityText   = findViewById(R.id.densityText);
        TextView  conditionText = findViewById(R.id.conditionText);
        TextView  observationText = findViewById(R.id.observationText);

        // Get data from intent
        String diagnosis     = getIntent().getStringExtra("diagnosis");
        String confidence    = getIntent().getStringExtra("confidence");
        String ratio         = getIntent().getStringExtra("ratio");
        String density       = getIntent().getStringExtra("density");
        String condition     = getIntent().getStringExtra("condition");
        String observation   = getIntent().getStringExtra("observation");
        String imageUriString = getIntent().getStringExtra("imageUri");

        // Populate UI - Main result
        if (diagnosis != null) {
            nameText.setText("Status : " + (diagnosis.contains("Normal") ? "Normal Scalp" : "Potential AGA"));
        } else {
            nameText.setText("Status : Unknown");
        }

        // Hair Density
        if (density != null) {
            densityText.setText("Hair Density : " + density.toUpperCase());
        } else if (confidence != null) {
            try {
                float confVal = Float.parseFloat(confidence);
                densityText.setText(String.format("AGA Probability : %.2f%%", confVal * 100));
            } catch (NumberFormatException e) {
                densityText.setText("Confidence : " + confidence);
            }
        }

        // Scalp Condition + Ratio
        StringBuilder condSb = new StringBuilder();
        if (condition != null) {
            condSb.append("Scalp Condition : ").append(condition.toUpperCase());
        }
        if (ratio != null) {
            if (condSb.length() > 0) condSb.append("  |  ");
            condSb.append("Ratio: ").append(ratio);
        }
        conditionText.setText(condSb.length() > 0 ? condSb.toString() : "Scalp Condition : Analysis Complete");

        // Observation
        if (observation != null) {
            observationText.setText(observation);
        } else {
            observationText.setText("Analysis complete. Please consult a specialist for further confirmation.");
        }

        // Image
        if (imageUriString != null) {
            resultImage.setImageURI(Uri.parse(imageUriString));
        }
    }
}
