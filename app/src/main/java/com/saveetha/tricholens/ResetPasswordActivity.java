package com.saveetha.tricholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.saveetha.tricholens.data.repository.UserRepository;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        ImageView backArrow = findViewById(R.id.backArrow);
        EditText newPassword = findViewById(R.id.newPassword);
        EditText confirmPassword = findViewById(R.id.confirmPassword);
        Button resetPasswordButton = findViewById(R.id.resetPasswordButton);

        backArrow.setOnClickListener(v -> finish());

        resetPasswordButton.setOnClickListener(v -> {
            String password = newPassword.getText().toString().trim();
            String confirm = confirmPassword != null ? confirmPassword.getText().toString().trim() : password;
            
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your new password", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                String phone = getIntent().getStringExtra("phone");
                if (phone == null || phone.isEmpty()) {
                    Toast.makeText(this, "Error: Missing phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Call UserRepository to reset password
                UserRepository repository = new UserRepository(this);
                repository.resetPassword(phone, password, new UserRepository.ResetPasswordCallback() {
                    @Override
                    public void onSuccess(JsonObject response) {
                        Toast.makeText(ResetPasswordActivity.this, "Password Successfully Reset!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ResetPasswordActivity.this, ResetSuccessfulActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ResetPasswordActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
