package com.example.weather.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HourlyItem {
    private long dt;
    private Main main;
    private List<Weather> weather;
    private Wind wind;
    private Rain rain;         // ← thêm mới (có thể null nếu không mưa)
    private String dt_txt;

    public long getDt()               { return dt; }
    public Main getMain()             { return main; }
    public List<Weather> getWeather() { return weather; }
    public Wind getWind()             { return wind; }
    public Rain getRain()             { return rain; }   // ← getter mới
    public String getDt_txt()         { return dt_txt; }

    // ── Inner class Rain ────────────────────────────────────────────────────
    public static class Rain {
        @SerializedName("3h")   // JSON key là "3h"
        private float threeHour;

        public float get3h() { return threeHour; }
    }
}