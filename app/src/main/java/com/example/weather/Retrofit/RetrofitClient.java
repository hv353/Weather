package com.example.weather.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static RetrofitClient instance;
    private final Retrofit retrofit;

    // Dùng Singleton pattern đúng chuẩn
    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public Retrofit getClient() {
        return retrofit;
    }
}