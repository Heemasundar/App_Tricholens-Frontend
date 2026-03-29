package com.saveetha.tricholens.network;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

        @FormUrlEncoded
        @POST("signup")
        Call<JsonObject> signup(
                        @Field("name") String name,
                        @Field("email") String email,
                        @Field("mobile") String mobile,
                        @Field("dob") String dob,
                        @Field("gender") String gender,
                        @Field("age") String age,
                        @Field("password") String password);

        @FormUrlEncoded
        @POST("login")
        Call<JsonObject> login(
                        @Field("username") String username,
                        @Field("password") String password);

        @FormUrlEncoded
        @POST("update_profile")
        Call<JsonObject> updateProfile(
                        @Field("email") String email,
                        @Field("name") String name,
                        @Field("mobile") String mobile,
                        @Field("dob") String dob,
                        @Field("gender") String gender,
                        @Field("age") String age);

        @FormUrlEncoded
        @POST("check_mobile")
        Call<JsonObject> checkMobile(
                        @Field("mobile") String mobile);

        @FormUrlEncoded
        @POST("reset_password")
        Call<JsonObject> resetPassword(
                        @Field("mobile") String mobile,
                        @Field("password") String password);

        @retrofit2.http.Multipart
        @POST("diagnose")
        Call<JsonObject> diagnose(
                        @retrofit2.http.Part okhttp3.MultipartBody.Part image);

        @FormUrlEncoded
        @POST("save_history")
        Call<JsonObject> saveHistory(
                        @Field("user_id") int userId,
                        @Field("diagnosis_result") String diagnosisResult,
                        @Field("image_path") String imagePath,
                        @Field("confidence") String confidence,
                        @Field("density") String density,
                        @Field("ratio") String ratio,
                        @Field("condition") String condition,
                        @Field("observation") String observation);

        @FormUrlEncoded
        @POST("get_history")
        Call<JsonObject> getHistory(
                        @Field("user_id") int userId);
}
