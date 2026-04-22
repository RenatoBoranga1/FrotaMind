package com.example.arlacontrole.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("auth/me")
    Call<AuthUserResponse> me();

    @POST("auth/logout")
    Call<Void> logout();

    @GET("healthz")
    Call<HealthResponse> healthcheck();

    @GET("api/vehicles")
    Call<List<VehicleResponse>> listVehicles();

    @GET("api/records")
    Call<List<RecordResponse>> listRecords();

    @POST("api/records")
    Call<RecordResponse> createRecord(@Body RecordCreateRequest request);
}
