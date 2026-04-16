package com.example.weather.Api;

import com.example.weather.Model.ForecastResponse;
import com.example.weather.Model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Call<WeatherResponse> getCurrentWeatherByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("forecast")
    Call<ForecastResponse> getHourlyForecastByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("cnt") int cnt
    );


}
