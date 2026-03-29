package com.saveetha.tricholens.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonObject;
import com.saveetha.tricholens.data.local.AppDatabase;
import com.saveetha.tricholens.data.local.UserDao;
import com.saveetha.tricholens.data.local.UserEntity;
import com.saveetha.tricholens.network.ApiClient;
import com.saveetha.tricholens.network.ApiService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private ApiService apiService;
    private UserDao userDao;
    private ExecutorService executor;
    private Handler mainHandler;

    public UserRepository(Context context) {
        this.apiService = ApiClient.getApiService();
        this.userDao = AppDatabase.getDatabase(context).userDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ============ CALLBACKS ============
    public interface LoginCallback {
        void onSuccess(JsonObject response);

        void onError(String message);
    }

    public interface SignupCallback {
        void onSuccess(JsonObject response);

        void onError(String message);
    }

    public interface UpdateProfileCallback {
        void onSuccess(JsonObject response);

        void onError(String message);
    }

    public interface CheckMobileCallback {
        void onSuccess(String status); // "exists" or "not_found"

        void onError(String message);
    }

    public interface ResetPasswordCallback {
        void onSuccess(JsonObject response);

        void onError(String message);
    }

    public interface GetHistoryCallback {
        void onSuccess(JsonObject response);

        void onError(String message);
    }

    public interface SaveHistoryCallback {
        void onSuccess(JsonObject response);

        void onError(String message);
    }

    // ============ LOGIN ============
    public void login(String username, String password, LoginCallback callback) {
        // 1. Online Attempt
        apiService.login(username, password).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("status") && body.get("status").getAsString().equals("success")) {

                        // Save to DB in background
                        // FIX: Run callback on Main Thread AFTER DB insert to avoid race condition
                        // FIX: Check !isJsonNull() to prevent UnsupportedOperationException
                        JsonObject userJson = body.getAsJsonObject("user");
                        executor.execute(() -> {
                            try {
                                String gender = (userJson.has("gender") && !userJson.get("gender").isJsonNull())
                                        ? userJson.get("gender").getAsString()
                                        : "";
                                String age = (userJson.has("age") && !userJson.get("age").isJsonNull())
                                        ? userJson.get("age").getAsString()
                                        : "";
                                String dob = (userJson.has("dob") && !userJson.get("dob").isJsonNull())
                                        ? userJson.get("dob").getAsString()
                                        : "";

                                UserEntity entity = new UserEntity(
                                        userJson.get("id").getAsInt(),
                                        userJson.get("name").getAsString(),
                                        userJson.get("email").getAsString(),
                                        userJson.get("mobile").getAsString(),
                                        dob,
                                        gender,
                                        age,
                                        System.currentTimeMillis());
                                userDao.insertUser(entity);

                                // Post success to main thread only after DB save
                                mainHandler.post(() -> callback.onSuccess(body));
                            } catch (Exception e) {
                                e.printStackTrace();
                                mainHandler.post(() -> callback.onError("Login Error: " + e.getMessage()));
                            }
                        });
                    } else {
                        callback.onError("Invalid Credentials");
                    }
                } else {
                    if (response.code() == 401 || response.code() == 400) {
                        // Common cause for 401 in this app is "Invalid credentials"
                        callback.onError("Invalid Username or Password");
                    } else {
                        String errorMsg = "Server Error: " + response.message();
                        try {
                            if (response.errorBody() != null) {
                                // Attempt to capture more detail if possible, though strict parsing isn't
                                // needed just for the message
                                // We'll stick to the status message + code for other errors to be safe
                                errorMsg += " (" + response.code() + ")";
                            }
                        } catch (Exception e) {
                            // keep default errorMsg
                        }
                        callback.onError(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // 2. Offline Fallback
                executor.execute(() -> {
                    UserEntity localUser = userDao.getLastLoggedInUser();
                    mainHandler.post(() -> {
                        if (localUser != null && localUser.email.equals(username)) {
                            // Mock success
                            JsonObject mockResponse = new JsonObject();
                            mockResponse.addProperty("status", "success");
                            mockResponse.addProperty("message", "Logged in offline");

                            JsonObject userJson = new JsonObject();
                            userJson.addProperty("id", localUser.id);
                            userJson.addProperty("name", localUser.name);
                            userJson.addProperty("mobile", localUser.mobile);
                            // userJson.addProperty("country", localUser.country); // REMOVED
                            mockResponse.add("user", userJson);

                            callback.onSuccess(mockResponse);
                        } else {
                            callback.onError(
                                    "Unable to connect to server and no offline data available. " + t.getMessage());
                        }
                    });
                });
            }
        });
    }

    // ============ SIGNUP ============
    public void signup(String name, String email, String mobile, String dob, String gender, String age, String password,
            SignupCallback callback) {
        apiService.signup(name, email, mobile, dob, gender, age, password).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("status") && body.get("status").getAsString().equals("success")) {
                        callback.onSuccess(body);
                    } else {
                        String message = body.has("message") ? body.get("message").getAsString() : "Signup Failed";
                        callback.onError(message);
                    }
                } else {
                    callback.onError("Server Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Connection Error: " + t.getMessage());
            }
        });
    }

    // ============ CHECK MOBILE ============
    public void checkMobile(String mobile, CheckMobileCallback callback) {
        apiService.checkMobile(mobile).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("status")) {
                        String status = body.get("status").getAsString();
                        callback.onSuccess(status); // "exists" or "not_found"
                    } else {
                        callback.onError("Invalid response from server");
                    }
                } else {
                    callback.onError("Server Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Connection Error: " + t.getMessage());
            }
        });
    }

    // ============ RESET PASSWORD ============
    public void resetPassword(String mobile, String password, ResetPasswordCallback callback) {
        apiService.resetPassword(mobile, password).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("status") && body.get("status").getAsString().equals("success")) {
                        callback.onSuccess(body);
                    } else {
                        String message = body.has("message") ? body.get("message").getAsString() : "Reset Failed";
                        callback.onError(message);
                    }
                } else {
                    callback.onError("Server Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Connection Error: " + t.getMessage());
            }
        });
    }

    // ============ UPDATE PROFILE ============
    public void updateProfile(String email, String name, String mobile, String dob, String gender, String age,
            UpdateProfileCallback callback) {
        apiService.updateProfile(email, name, mobile, dob, gender, age).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("status") && body.get("status").getAsString().equals("success")) {
                        // Update local database
                        JsonObject userJson = body.getAsJsonObject("user");
                        executor.execute(() -> {
                            UserEntity entity = new UserEntity(
                                    userJson.get("id").getAsInt(),
                                    userJson.get("name").getAsString(),
                                    userJson.get("email").getAsString(),
                                    userJson.get("mobile").getAsString(),
                                    userJson.has("dob") ? userJson.get("dob").getAsString() : "",
                                    userJson.has("gender") ? userJson.get("gender").getAsString() : "",
                                    userJson.has("age") ? userJson.get("age").getAsString() : "",
                                    System.currentTimeMillis());
                            userDao.insertUser(entity);
                        });
                        callback.onSuccess(body);
                    } else {
                        String message = body.has("message") ? body.get("message").getAsString() : "Update Failed";
                        callback.onError(message);
                    }
                } else {
                    callback.onError("Server Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Connection Error: " + t.getMessage());
            }
        });
    }

    // ============ HISTORY ============
    public void getHistory(int userId, GetHistoryCallback callback) {
        apiService.getHistory(userId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("status") && body.get("status").getAsString().equals("success")) {
                        callback.onSuccess(body);
                    } else {
                        String message = body.has("message") ? body.get("message").getAsString() : "Failed to get history";
                        callback.onError(message);
                    }
                } else {
                    callback.onError("Server Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Connection Error: " + t.getMessage());
            }
        });
    }

    public void saveHistory(int userId, String diagnosisResult, String imagePath, String confidence, String density, String ratio, String condition, String observation, SaveHistoryCallback callback) {
        apiService.saveHistory(userId, diagnosisResult, imagePath, confidence, density, ratio, condition, observation).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("status") && body.get("status").getAsString().equals("success")) {
                        callback.onSuccess(body);
                    } else {
                        String message = body.has("message") ? body.get("message").getAsString() : "Failed to save history";
                        callback.onError(message);
                    }
                } else {
                    callback.onError("Server Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Connection Error: " + t.getMessage());
            }
        });
    }

    // ============ LOGOUT ============
    public void logout(Runnable onComplete) {
        executor.execute(() -> {
            userDao.clearAll();
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    // ============ GET LAST LOGGED IN USER (for UI display) ============
    public UserEntity getLastLoggedInUserSync() {
        return userDao.getLastLoggedInUser();
    }
}
