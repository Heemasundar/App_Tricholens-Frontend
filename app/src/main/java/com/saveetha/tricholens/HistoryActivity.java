package com.saveetha.tricholens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        // 🔙 Back Arrow
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> finish());

        // 🔍 Search
        EditText searchEditText = findViewById(R.id.searchEditText);

        // 📌 TextViews for names and dates
        TextView userName1 = findViewById(R.id.tvUserName1);
        TextView date1 = findViewById(R.id.tvDate1);

        TextView userName4 = findViewById(R.id.tvUserName4);
        TextView date4 = findViewById(R.id.tvDate4);

        TextView userName5 = findViewById(R.id.tvUserName5);
        TextView date5 = findViewById(R.id.tvDate5);

        TextView userName6 = findViewById(R.id.tvUserName6);
        TextView date6 = findViewById(R.id.tvDate6);

        // Fetch History from Backend
        userName1.setText("Loading...");
        date1.setText("");
        userName2.setText("");
        date2.setText("");
        userName3.setText("");
        date3.setText("");
        userName4.setText("");
        date4.setText("");
        userName5.setText("");
        date5.setText("");
        userName6.setText("");
        date6.setText("");

        new Thread(() -> {
            com.saveetha.tricholens.data.repository.UserRepository repository = new com.saveetha.tricholens.data.repository.UserRepository(HistoryActivity.this);
            com.saveetha.tricholens.data.local.UserEntity user = repository.getLastLoggedInUserSync();
            if (user != null) {
                repository.getHistory(user.id, new com.saveetha.tricholens.data.repository.UserRepository.GetHistoryCallback() {
                    @Override
                    public void onSuccess(com.google.gson.JsonObject response) {
                        try {
                            if (response.has("history")) {
                                com.google.gson.JsonArray historyArray = response.getAsJsonArray("history");
                                runOnUiThread(() -> {
                                    userName1.setText(historyArray.size() == 0 ? "No history found" : "");

                                    TextView[] userNames = {userName1, userName2, userName3, userName4, userName5, userName6};
                                    TextView[] dates = {date1, date2, date3, date4, date5, date6};

                                    for (int i = 0; i < historyArray.size() && i < 6; i++) {
                                        final com.google.gson.JsonObject hist = historyArray.get(i).getAsJsonObject();
                                        String diag = hist.has("diagnosis_result") && !hist.get("diagnosis_result").isJsonNull() ? hist.get("diagnosis_result").getAsString() : "Unknown";
                                        String dDate = hist.has("diagnosis_date") && !hist.get("diagnosis_date").isJsonNull() ? hist.get("diagnosis_date").getAsString() : "";
                                        String img = hist.has("image_path") && !hist.get("image_path").isJsonNull() ? hist.get("image_path").getAsString() : "";
                                        String conf = hist.has("confidence") && !hist.get("confidence").isJsonNull() ? hist.get("confidence").getAsString() : "";
                                        String dens = hist.has("density") && !hist.get("density").isJsonNull() ? hist.get("density").getAsString() : "";
                                        String rat = hist.has("ratio") && !hist.get("ratio").isJsonNull() ? hist.get("ratio").getAsString() : "";
                                        String cond = hist.has("condition") && !hist.get("condition").isJsonNull() ? hist.get("condition").getAsString() : "";
                                        String obs = hist.has("observation") && !hist.get("observation").isJsonNull() ? hist.get("observation").getAsString() : "";

                                        userNames[i].setText(diag);
                                        dates[i].setText(dDate);

                                        userNames[i].setOnClickListener(v -> {
                                            Intent intent = new Intent(HistoryActivity.this, ObjectDetectedActivity.class);
                                            intent.putExtra("diagnosis", diag);
                                            intent.putExtra("confidence", conf);
                                            intent.putExtra("ratio", rat);
                                            intent.putExtra("density", dens);
                                            intent.putExtra("condition", cond);
                                            intent.putExtra("observation", obs);
                                            intent.putExtra("imageUri", img);
                                            startActivity(intent);
                                        });
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            userName1.setText("Error loading history");
                            android.widget.Toast.makeText(HistoryActivity.this, "Error: " + message, android.widget.Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        }).start();

        // 🔽 Bottom Navigation
        LinearLayout bottomNav = findViewById(R.id.bottomNavigation);
        View profileSection = bottomNav.getChildAt(0);
        View homeSection = bottomNav.getChildAt(1); // Assuming 0 is Profile, 1 is Home based on Kotlin code access
                                                    // logic?
        // Kotlin Code: getChildAt(0) -> Profile, getChildAt(1) -> Home.
        // Wait, Kotlin code said:
        // val profileSection =
        // findViewById<LinearLayout>(R.id.bottomNavigation).getChildAt(0)
        // val homeSection =
        // findViewById<LinearLayout>(R.id.bottomNavigation).getChildAt(1)
        // Usually index 0 is left, 1 is right. If Home is middle/left, this might be
        // swapped.
        // I will follow the exact index from Kotlin.

        if (profileSection != null) {
            profileSection.setOnClickListener(v -> startActivity(new Intent(this, ViewProfileActivity.class)));
        }

        if (homeSection != null) {
            homeSection.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        }
    }
}
