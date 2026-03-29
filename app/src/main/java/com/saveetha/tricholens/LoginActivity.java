package com.saveetha.tricholens;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.saveetha.tricholens.data.repository.UserRepository;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // GLOBAL CRASH HANDLER
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            e.printStackTrace();
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Toast.makeText(getApplicationContext(), "CRITICAL ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            System.exit(1);
        });

        try {
            setContentView(R.layout.activity_login);
            Toast.makeText(this, "VERIFICATION: V2_LOGIN_SCREEN_ACTIVE", Toast.LENGTH_LONG).show();

            // Animation
            try {
                Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

                findViewById(R.id.main_content).startAnimation(slideUp);
                findViewById(R.id.logoImage).startAnimation(fadeIn);
            } catch (Exception e) {
                e.printStackTrace();
            }

            EditText editUsername = findViewById(R.id.editUsername);
            EditText editPassword = findViewById(R.id.editPassword);
            Button signInButton = findViewById(R.id.signInButton);
            TextView forgotPassword = findViewById(R.id.forgotPassword);
            TextView signUpText = findViewById(R.id.signUpText);

            signInButton.setOnClickListener(v -> {
                try {
                    String username = editUsername.getText().toString().trim(); // Trim added for safety
                    String password = editPassword.getText().toString().trim();

                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Please enter phone number and password", Toast.LENGTH_SHORT).show();
                    } else {
                        UserRepository repository = new UserRepository(this);
                        repository.login(username, password, new UserRepository.LoginCallback() {
                            @Override
                            public void onSuccess(JsonObject response) {
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(this, "Crash Prevented: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // Forgot Password
            if (forgotPassword != null) {
                forgotPassword
                        .setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordPhoneActivity.class)));
            }

            // Signup (Clicking the text or a safe way to trigger)
            if (signUpText != null) {
                signUpText.setOnClickListener(v -> {
                    try {
                        startActivity(new Intent(this, SignupActivity.class));
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(this, "Error opening Signup: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Throwable t) {
            t.printStackTrace();
            // Try to show toast even if layout failed
            Toast.makeText(getApplicationContext(), "Startup Fatal Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
