package com.example.weather.Model;

import java.util.ArrayList;
import java.util.List;

public class WeatherResponse {
    private List<Weather> weather;
    private Main main;
    private Wind wind;
    private String name;

    public List<Weather> getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public String getName() {
        return name;
    }
}


