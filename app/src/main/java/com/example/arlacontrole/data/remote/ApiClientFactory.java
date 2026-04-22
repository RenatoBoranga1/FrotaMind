package com.example.arlacontrole.data.remote;

import com.example.arlacontrole.utils.AppPreferences;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClientFactory {

    private ApiClientFactory() {
    }

    public static ApiService create(String baseUrl, AppPreferences preferences) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(chain -> {
                Request original = chain.request();
                String accessToken = preferences == null ? "" : preferences.getAccessToken();
                if (accessToken == null || accessToken.trim().isEmpty()) {
                    return chain.proceed(original);
                }
                Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
                return chain.proceed(request);
            })
            .addInterceptor(loggingInterceptor)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        return retrofit.create(ApiService.class);
    }

    public static ApiService create(String baseUrl) {
        return create(baseUrl, null);
    }
}
