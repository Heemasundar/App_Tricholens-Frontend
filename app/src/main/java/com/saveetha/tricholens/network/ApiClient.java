package com.saveetha.tricholens.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // For Emulator: 10.0.2.2 = localhost
    // For Real Device: Replace with your actual machine IP (e.g., 192.168.x.x)
    // Backend runs on port 8000, 5000, 8001, or 8080 (whichever is available)

    // CHANGE THIS FOR YOUR DEVICE/NETWORK:
    // Your current machine IP (from ipconfig) is 10.195.39.216. Port is 5000.
    private static final String BASE_URL = "http://10.211.142.216:5000/"; // Final Redirection to Local

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
