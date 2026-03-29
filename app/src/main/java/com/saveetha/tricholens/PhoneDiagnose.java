package com.saveetha.tricholens;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.saveetha.tricholens.network.ApiClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneDiagnose extends AppCompatActivity {

    private Uri selectedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        ImageView scalpImageView = findViewById(R.id.scalpImageView);
        Button diagnoseButton    = findViewById(R.id.diagnoseButton);

        // Load image passed from previous screen
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            selectedUri = Uri.parse(imageUriString);
            scalpImageView.setImageURI(selectedUri);
        }

        diagnoseButton.setOnClickListener(v -> {
            if (selectedUri != null) {
                uploadImageAndDiagnose(selectedUri);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageAndDiagnose(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Analyzing image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            InputStream iStream = getContentResolver().openInputStream(imageUri);
            byte[] inputData = getBytes(iStream);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), inputData);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "scalp_image.jpg", requestFile);

            ApiClient.getApiService().diagnose(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json    = response.body();
                        String status      = json.has("status")    ? json.get("status").getAsString()    : "error";
                        String diagnosis   = json.has("diagnosis") ? json.get("diagnosis").getAsString() : "Unknown";
                        String confidence  = json.has("confidence") ? json.get("confidence").getAsString() : "0.0";
                        String ratio       = json.has("miniaturized_hair_ratio") ? json.get("miniaturized_hair_ratio").getAsString() : "-";
                        String density     = json.has("density")         ? json.get("density").getAsString()         : "Normal";
                        String condition   = json.has("scalp_condition")  ? json.get("scalp_condition").getAsString()  : "Healthy";
                        String observation = json.has("educational_observation") ? json.get("educational_observation").getAsString() : diagnosis;

                        if ("valid".equals(status) || "success".equals(status)) {
                             // Save history in background (final copies for lambda if needed, 
                             // though since they are local and not modified they should be effectively final)
                             new Thread(() -> {
                                 com.saveetha.tricholens.data.repository.UserRepository repo =
                                         new com.saveetha.tricholens.data.repository.UserRepository(PhoneDiagnose.this);
                                 com.saveetha.tricholens.data.local.UserEntity user = repo.getLastLoggedInUserSync();
                                 if (user != null) {
                                     repo.saveHistory(user.id, diagnosis, imageUri.toString(), confidence, density, ratio, condition, observation, new com.saveetha.tricholens.data.repository.UserRepository.SaveHistoryCallback() {
                                         @Override
                                         public void onSuccess(JsonObject r) {
                                             runOnUiThread(() -> Toast.makeText(PhoneDiagnose.this, "History saved successfully!", Toast.LENGTH_SHORT).show());
                                         }
                                         @Override
                                         public void onError(String m) {
                                             runOnUiThread(() -> Toast.makeText(PhoneDiagnose.this, "Failed to save history: " + m, Toast.LENGTH_LONG).show());
                                         }
                                     });
                                 }
                             }).start();

                            Intent intent = new Intent(PhoneDiagnose.this, ObjectDetectedActivity.class);
                            intent.putExtra("diagnosis",   diagnosis);
                            intent.putExtra("confidence",  confidence);
                            intent.putExtra("ratio",       ratio);
                            intent.putExtra("density",     density);
                            intent.putExtra("condition",   condition);
                            intent.putExtra("observation", observation);
                            intent.putExtra("imageUri",    imageUri.toString());
                            startActivity(intent);
                            finish();

                        } else if ("invalid".equals(status) || "invalid_image".equals(status)) {
                            String msg = json.has("message") ? json.get("message").getAsString() : "Invalid Image. Not a scalp image.";
                            showErrorDialog("Invalid Image", msg);

                        } else if ("inconclusive".equals(status)) {
                            String msg = json.has("message") ? json.get("message").getAsString() : "Inconclusive results due to poor image quality.";
                            showErrorDialog("Poor Quality", msg);

                        } else {
                            String msg = json.has("message") ? json.get("message").getAsString() : "Unexpected server response.";
                            Toast.makeText(PhoneDiagnose.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(PhoneDiagnose.this, "Server Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(PhoneDiagnose.this, "Connection Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
