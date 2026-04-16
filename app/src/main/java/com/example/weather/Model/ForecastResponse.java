package com.example.weather.Model;

import java.util.List;

public class ForecastResponse {
    private List<HourlyItem> list; // Danh sách các mốc thời gian (mỗi 3 giờ)
    private City city;

    public List<HourlyItem> getList() {
        return list;
    }
    public City getCity() {
        return city;
    }
}
