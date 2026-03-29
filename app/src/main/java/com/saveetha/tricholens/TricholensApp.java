package com.saveetha.tricholens;

import android.app.Application;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

public class TricholensApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // ULTIMATE CRASH HANDLER: Intercepts ALL crashes application-wide
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            e.printStackTrace(); // Log stack trace

            // Show Toast on Main Thread
            new Handler(Looper.getMainLooper()).post(() -> {
                // Use a very long toast so user can read it
                Toast.makeText(getApplicationContext(), "FATAL APP ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });

            // Wait for Toast to show before dying
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }

            // Kill process cleanly
            System.exit(1);
        });
    }
}
