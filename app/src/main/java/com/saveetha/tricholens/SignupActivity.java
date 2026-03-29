package com.saveetha.tricholens;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.saveetha.tricholens.data.repository.UserRepository;

import java.util.Calendar;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // GLOBAL CRASH HANDLER
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            e.printStackTrace();
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Toast.makeText(getApplicationContext(), "SIGNUP CRITICAL: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            System.exit(1);
        });

        super.onCreate(savedInstanceState);

        try {
            // TEST MODE: Try to inflate XML, but fallback to code only if it fails
            try {
                setContentView(R.layout.activity_signup);
            } catch (Throwable layoutError) {
                // FALLBACK TO SAFE MODE
                layoutError.printStackTrace();
                Toast.makeText(this, "Layout Crash! Switching to Safe Mode.", Toast.LENGTH_LONG).show();

                android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
                layout.setOrientation(android.widget.LinearLayout.VERTICAL);
                layout.setGravity(android.view.Gravity.CENTER);
                layout.setPadding(32, 32, 32, 32);

                TextView title = new TextView(this);
                title.setText("SIGNUP SAFE MODE");
                title.setTextSize(24);
                title.setGravity(android.view.Gravity.CENTER);
                title.setTypeface(null, android.graphics.Typeface.BOLD);
                layout.addView(title);

                TextView errorText = new TextView(this);
                errorText.setText("Error: " + layoutError.getMessage());
                errorText.setTextColor(android.graphics.Color.RED);
                errorText.setTextSize(14);
                errorText.setGravity(android.view.Gravity.CENTER);
                errorText.setPadding(0, 20, 0, 0);
                layout.addView(errorText);

                setContentView(layout);
                return; // Stop initialization
            }

            try {
                // Animation
                try {
                    // AnimationUtils.loadAnimation might crash if context is weird
                    Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
                    Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

                    if (findViewById(R.id.main_content) != null)
                        findViewById(R.id.main_content).startAnimation(slideUp);
                    if (findViewById(R.id.logoImage) != null)
                        findViewById(R.id.logoImage).startAnimation(fadeIn);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                EditText editName = findViewById(R.id.editName);
                EditText editEmail = findViewById(R.id.editEmail);
                EditText editMobile = findViewById(R.id.editMobile);
                EditText editPassword = findViewById(R.id.editPassword);
                EditText editDob = findViewById(R.id.editDob);

                EditText editGender = findViewById(R.id.editGender);
                EditText editAge = findViewById(R.id.editAge);

                Button signUpButton = findViewById(R.id.signUpButton);
                TextView signInText = findViewById(R.id.signInText);

                // NULL CHECKS
                if (editName == null || signUpButton == null) {
                    throw new RuntimeException("Vital Views Missing in Layout!");
                }

                // Setup Gender Selection
                if (editGender != null) {
                    editGender.setOnClickListener(v -> {
                        String[] genders = { "Male", "Female", "Other" };
                        new AlertDialog.Builder(this)
                                .setTitle("Select Gender")
                                .setItems(genders, (dialog, which) -> {
                                    editGender.setText(genders[which]);
                                    editGender.setError(null);
                                })
                                .show();
                    });
                }

                // Setup Age Selection (10 to 99)
                if (editAge != null) {
                    editAge.setOnClickListener(v -> {
                        String[] ages = new String[90];
                        for (int i = 0; i < 90; i++) {
                            ages[i] = String.valueOf(i + 10);
                        }
                        new AlertDialog.Builder(this)
                                .setTitle("Select Age")
                                .setItems(ages, (dialog, which) -> {
                                    editAge.setText(ages[which]);
                                    editAge.setError(null);
                                })
                                .show();
                    });
                }

                // Date Picker for DOB
                if (editDob != null) {
                    editDob.setOnClickListener(v -> {
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(SignupActivity.this,
                                (view, year1, monthOfYear, dayOfMonth) -> {
                                    String date = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                    editDob.setText(date);
                                    editDob.setError(null);
                                }, year, month, day);
                        datePickerDialog.show();
                    });
                }

                if (signUpButton != null) {
                    signUpButton.setOnClickListener(v -> {
                        try {
                            String name = editName.getText().toString().trim();
                            String email = editEmail.getText().toString().trim();
                            String mobile = editMobile.getText().toString().trim();
                            String password = editPassword.getText().toString().trim();
                            String dob = editDob.getText().toString().trim();

                            String gender = editGender != null ? editGender.getText().toString().trim() : "";
                            String age = editAge != null ? editAge.getText().toString().trim() : "";

                            if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty()
                                    || dob.isEmpty()) {
                                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (gender.isEmpty() || gender.equals("Select Gender")) {
                                if (editGender != null)
                                    editGender.setError("Required");
                                Toast.makeText(this, "Please select a valid gender", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (age.isEmpty() || age.equals("Select Age")) {
                                if (editAge != null)
                                    editAge.setError("Required");
                                Toast.makeText(this, "Please select a valid age", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (!email.contains("@")) {
                                editEmail.setError("Invalid Email");
                                return;
                            }

                            UserRepository repository = new UserRepository(this);
                            repository.signup(name, email, mobile, dob, gender, age, password,
                                    new UserRepository.SignupCallback() {
                                        @Override
                                        public void onSuccess(JsonObject response) {
                                            Toast.makeText(SignupActivity.this, "Registration Successful!",
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                            finish();
                                        }

                                        @Override
                                        public void onError(String message) {
                                            Toast.makeText(SignupActivity.this, "Error: " + message, Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    });
                        } catch (Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(this, "Crash prevented: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                if (signInText != null) {
                    signInText.setOnClickListener(v -> {
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    });
                }
            } catch (Throwable t) {
                t.printStackTrace();
                Toast.makeText(this, "Signup Init Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }

        } catch (Throwable t) {
            t.printStackTrace();
            // Try to show toast even if layout failed
            Toast.makeText(getApplicationContext(), "Fatal Signup Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
