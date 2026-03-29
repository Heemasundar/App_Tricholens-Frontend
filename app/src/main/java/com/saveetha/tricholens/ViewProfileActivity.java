package com.saveetha.tricholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.saveetha.tricholens.data.local.UserEntity;
import com.saveetha.tricholens.data.repository.UserRepository;

public class ViewProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        ImageView backIcon = findViewById(R.id.backIcon);
        TextView nameText = findViewById(R.id.nameText);
        TextView emailText = findViewById(R.id.emailText);
        TextView phoneText = findViewById(R.id.phoneText);
        TextView dobText = findViewById(R.id.dobText);
        TextView ageText = findViewById(R.id.ageText);
        TextView genderText = findViewById(R.id.genderText);
        // TextView countryText = findViewById(R.id.countryText); // REMOVED

        // Back button click
        backIcon.setOnClickListener(v -> finish());

        // Load user data from local database
        UserRepository repository = new UserRepository(this);
        UserEntity user = repository.getLastLoggedInUserSync();

        if (user != null) {
            nameText.setText(user.name != null ? user.name : "N/A");
            emailText.setText(user.email != null ? user.email : "N/A");
            phoneText.setText(user.mobile != null ? user.mobile : "N/A");
            dobText.setText((user.dob != null && !user.dob.isEmpty()) ? user.dob : "N/A");
            ageText.setText(user.age != null ? user.age : "N/A");
            genderText.setText(user.gender != null ? user.gender : "N/A");
            // countryText.setText(user.country != null ? user.country : "N/A");
        }

        // Action Buttons
        android.widget.Button updateProfileButton = findViewById(R.id.updateProfileButton);
        android.widget.Button logoutButton = findViewById(R.id.logoutButton);

        updateProfileButton.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, UpdateProfileActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            // Confirm Logout
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        repository.logout(() -> {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
}
