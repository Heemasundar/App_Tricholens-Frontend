package com.saveetha.tricholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.saveetha.tricholens.data.repository.UserRepository;

public class ForgotPasswordPhoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_phone);

        ImageView backArrow = findViewById(R.id.backArrow);
        EditText editPhone = findViewById(R.id.editPhone);
        Button continueButton = findViewById(R.id.continueButton);

        backArrow.setOnClickListener(v -> finish());

        continueButton.setOnClickListener(v -> {
            String phone = editPhone.getText().toString().trim();

            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            } else if (phone.length() < 10) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            } else {
                // Check if mobile exists in database
                UserRepository repository = new UserRepository(this);
                repository.checkMobile(phone, new UserRepository.CheckMobileCallback() {
                    @Override
                    public void onSuccess(String status) {
                        if ("exists".equals(status)) {
                            // Phone found, proceed to OTP verification
                            Intent intent = new Intent(ForgotPasswordPhoneActivity.this,
                                    OtpVerificationPhoneActivity.class);
                            intent.putExtra("phone", phone);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ForgotPasswordPhoneActivity.this, "Phone number not registered", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ForgotPasswordPhoneActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
