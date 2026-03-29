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
import com.saveetha.tricholens.data.repository.UserRepository;

import java.util.Calendar;

public class SignupProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_profile);

        ImageView backArrow = findViewById(R.id.backArrow);
        EditText editName = findViewById(R.id.editName);
        EditText editEmail = findViewById(R.id.editEmail);
        EditText editMobile = findViewById(R.id.editMobile);
        EditText editDob = findViewById(R.id.editDob);
        // EditText editCountry = findViewById(R.id.editCountry); // REMOVED
        Button saveButton = findViewById(R.id.saveButton);

        // Get data passed from SignupActivity
        String intentMobile = getIntent().getStringExtra("mobile");
        String intentPassword = getIntent().getStringExtra("password");

        // Pre-fill mobile if available
        if (intentMobile != null) {
            editMobile.setText(intentMobile);
            editMobile.setEnabled(false); // Make it read-only
        }

        backArrow.setOnClickListener(v -> finish());

        // DatePicker for DOB
        editDob.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SignupProfile.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String date = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        editDob.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String mobile = editMobile.getText().toString().trim();
            String dob = editDob.getText().toString().trim();
            // String country = editCountry.getText().toString().trim(); // REMOVED
            // Use password from intent
            String password = (intentPassword != null) ? intentPassword : "";

            if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || dob.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Error: Password missing from previous screen", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call UserRepository for signup
            UserRepository repository = new UserRepository(this);
            // Passing dummy gender "Male" and age "25" to match new backend requirement
            repository.signup(name, email, mobile, dob, "Male", "25", password, new UserRepository.SignupCallback() {
                @Override
                public void onSuccess(JsonObject response) {
                    Toast.makeText(SignupProfile.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupProfile.this, LoginActivity.class));
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(SignupProfile.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
