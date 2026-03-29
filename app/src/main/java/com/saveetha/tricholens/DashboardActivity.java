package com.saveetha.tricholens;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.saveetha.tricholens.data.local.UserEntity;
import com.saveetha.tricholens.data.repository.UserRepository;

public class DashboardActivity extends AppCompatActivity {

    // Image Picker
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // TODO: Handle selected image here and send to backend for diagnosis
                        Intent intent = new Intent(this, PhoneDiagnose.class);
                        intent.putExtra("imageUri", selectedImageUri.toString());
                        startActivity(intent);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toast.makeText(this, "VERIFICATION: V2_SERVER_LOGS_ACTIVE", Toast.LENGTH_LONG).show();

        LinearLayout profileContainer = findViewById(R.id.profileContainer);
        LinearLayout uploadContainer = findViewById(R.id.uploadContainer);
        LinearLayout historyContainer = findViewById(R.id.historyContainer);
        TextView usernameText = findViewById(R.id.usernameText);

        // Animation
        try {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
            uploadContainer.startAnimation(slideUp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load and display logged-in user's name
        UserRepository repository = new UserRepository(this);
        UserEntity user = repository.getLastLoggedInUserSync();

        if (user != null) {
            usernameText.setText("Hello, " + user.name);
        } else {
            usernameText.setText("Hello, User");
            // Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show(); //
            // Suppress generic toast
        }

        // Profile (icon)
        profileContainer.setOnClickListener(v -> startActivity(new Intent(this, ViewProfileActivity.class)));

        // Upload image from gallery
        uploadContainer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            pickImage.launch(intent);
        });

        // Profile (text is part of header now, no click needed usually, but keeping
        // logic if desired)
        // usernameText.setOnClickListener(v -> startActivity(new Intent(this,
        // ViewProfileActivity.class)));

        // History
        historyContainer.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
    }
}
