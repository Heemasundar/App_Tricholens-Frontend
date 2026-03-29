package com.saveetha.tricholens;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.saveetha.tricholens.data.local.UserEntity;
import com.saveetha.tricholens.data.repository.UserRepository;

import java.util.Calendar;

public class UpdateProfileActivity extends AppCompatActivity {

    private UserRepository repository;
    private UserEntity currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_profile);

        repository = new UserRepository(this);
        currentUser = repository.getLastLoggedInUserSync();

        ImageView backArrow = findViewById(R.id.backArrow);
        EditText editName = findViewById(R.id.editName);
        EditText editEmail = findViewById(R.id.editEmail);
        EditText editMobile = findViewById(R.id.editMobile);
        EditText editDob = findViewById(R.id.editDob);
        // EditText editCountry = findViewById(R.id.editCountry); // REMOVED
        Button saveButton = findViewById(R.id.saveButton);

        // DatePicker for DOB
        editDob.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(UpdateProfileActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String date = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        editDob.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Pre-populate fields with current user data
        if (currentUser != null) {
            editName.setText(currentUser.name);
            editEmail.setText(currentUser.email);
            editMobile.setText(currentUser.mobile);
            editEmail.setEnabled(false); // Email should not be editable (used as identifier)
        } else {
            Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show();
        }

        backArrow.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String mobile = editMobile.getText().toString().trim();
            String dob = editDob.getText().toString().trim();
            // String country = editCountry.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Email is required to identify user", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call UserRepository to update profile
            // CORRECTED: Removed country argument and added age/gender placeholders if
            // needed or just correct method signature
            repository.updateProfile(email, name, mobile, dob, "Male", "25",
                    new UserRepository.UpdateProfileCallback() {
                        @Override
                        public void onSuccess(JsonObject response) {
                            Toast.makeText(UpdateProfileActivity.this, "Profile Updated Successfully!",
                                    Toast.LENGTH_SHORT)
                                    .show();
                            startActivity(new Intent(UpdateProfileActivity.this, DashboardActivity.class));
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(UpdateProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
