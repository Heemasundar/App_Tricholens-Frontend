package com.saveetha.tricholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OtpVerificationPhoneActivity extends AppCompatActivity {

    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification_phone);

        phone = getIntent().getStringExtra("phone");

        ImageView backArrow = findViewById(R.id.backArrow);
        EditText otp1 = findViewById(R.id.otp1);
        EditText otp2 = findViewById(R.id.otp2);
        EditText otp3 = findViewById(R.id.otp3);
        EditText otp4 = findViewById(R.id.otp4);
        Button verifyOtpButton = findViewById(R.id.verifyOtpButton);
        TextView resendText = findViewById(R.id.resendText);

        backArrow.setOnClickListener(v -> finish());

        verifyOtpButton.setOnClickListener(v -> {
            String otp = otp1.getText().toString() +
                    otp2.getText().toString() +
                    otp3.getText().toString() +
                    otp4.getText().toString();

            if (otp.length() < 4) {
                Toast.makeText(this, "Please enter the full OTP", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: In production, verify OTP with backend
                // For now, we proceed to password reset
                Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show();
                Intent nextIntent = new Intent(this, ResetPasswordActivity.class);
                nextIntent.putExtra("phone", phone);
                startActivity(nextIntent);
                finish();
            }
        });

        resendText.setOnClickListener(v -> 
            Toast.makeText(this, "OTP Resent to " + phone, Toast.LENGTH_SHORT).show()
        );
    }
}
