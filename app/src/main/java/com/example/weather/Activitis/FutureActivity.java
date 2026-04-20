package com.example.weather.Activitis;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.Adapters.FutureAdapters;
import com.example.weather.Api.WeatherApi;
import com.example.weather.Domains.FutureDomains;
import com.example.weather.Model.ForecastResponse;
import com.example.weather.Model.HourlyItem;
import com.example.weather.R;
import com.example.weather.Retrofit.RetrofitClient;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FutureActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapterTomorrow;

    private ImageView imgTomorrow;
    private TextView  txtTomorrowLabel;
    private TextView  txtTomorrowTemp;
    private TextView  txtTomorrowDesc;
    private TextView  txtRain;
    private TextView  txtWind;
    private TextView  txtHumidity;

    private static final String API_KEY = "37c6ba63ded1fc4c85bbb65eb2846d0d";

    // Tọa độ nhận từ MainActivity (truyền qua Intent)
    private double currentLat = 21.0285;
    private double currentLon = 105.8542;
    private SwitchMaterial switchUnit;
    private boolean isCelsius = true;

    // Lưu dữ liệu gốc (°C) để convert lại
    private int lastAvgTempC = 0;
    private ArrayList<FutureDomains> lastItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future);

        // Nhận tọa độ từ MainActivity
        Intent intent = getIntent();
        if (intent != null) {
            currentLat = intent.getDoubleExtra("lat", 21.0285);
            currentLon = intent.getDoubleExtra("lon", 105.8542);
        }

        recyclerView = findViewById(R.id.view2);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );

        imgTomorrow      = findViewById(R.id.imageView5);
        txtTomorrowLabel = findViewById(R.id.textView2);
        txtTomorrowTemp  = findViewById(R.id.textView3);
        txtTomorrowDesc  = findViewById(R.id.textView4);
        txtRain          = findViewById(R.id.txtRain);
        txtWind          = findViewById(R.id.txtWind);
        txtHumidity      = findViewById(R.id.txtHumidity);

        setVariable();
        loadDailyForecast();
    }

    // ── Lấy dự báo 5 ngày từ API (theo tọa độ) ───────────────────────────
    private void loadDailyForecast() {
        WeatherApi api = RetrofitClient.getInstance().getClient().create(WeatherApi.class);

        api.getHourlyForecastByCoords(currentLat, currentLon, API_KEY, "metric", 40)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call,
                                           Response<ForecastResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) return;

                        List<HourlyItem> forecastList = response.body().getList();

                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DAY_OF_YEAR, 1);
                        String tomorrowDate = new SimpleDateFormat(
                                "yyyy-MM-dd", Locale.getDefault()
                        ).format(cal.getTime());

                        Map<String, List<HourlyItem>> groupedByDay = new LinkedHashMap<>();
                        for (HourlyItem item : forecastList) {
                            String date = item.getDt_txt().substring(0, 10);
                            if (!groupedByDay.containsKey(date)) {
                                groupedByDay.put(date, new ArrayList<>());
                            }
                            groupedByDay.get(date).add(item);
                        }

                        // ── Bind Tomorrow header ───────────────────────────
                        if (groupedByDay.containsKey(tomorrowDate)) {
                            List<HourlyItem> tomorrowItems = groupedByDay.get(tomorrowDate);

                            float maxTemp = Float.MIN_VALUE, minTemp = Float.MAX_VALUE;
                            float totalRain = 0, totalWind = 0, totalHumid = 0;
                            String desc = tomorrowItems.get(0).getWeather().get(0).getDescription();

                            for (HourlyItem item : tomorrowItems) {
                                float t = item.getMain().getTemp();
                                if (t > maxTemp) maxTemp = t;
                                if (t < minTemp) minTemp = t;
                                totalWind  += item.getWind().getSpeed();
                                totalHumid += item.getMain().getHumidity();
                                if (item.getRain() != null) totalRain += item.getRain().get3h();
                            }

                            int   count    = tomorrowItems.size();
                            float avgWind  = totalWind  / count;
                            float avgHumid = totalHumid / count;
                            int   avgTemp  = (int) ((maxTemp + minTemp) / 2);

                            txtTomorrowLabel.setText(formatDate(tomorrowDate));
                            txtTomorrowTemp .setText(avgTemp + "°C");
                            txtTomorrowDesc .setText(capitalize(desc));
                            txtRain    .setText(String.format(Locale.getDefault(), "%.1f mm", totalRain));
                            txtWind    .setText(String.format(Locale.getDefault(), "%.1f km/h", avgWind * 3.6f));
                            txtHumidity.setText(String.format(Locale.getDefault(), "%.0f%%", avgHumid));
                            imgTomorrow.setImageResource(getWeatherDrawable(desc));
                            lastAvgTempC = avgTemp;

                        }

                        // ── RecyclerView (bỏ hôm nay) ─────────────────────
                        ArrayList<FutureDomains> items = new ArrayList<>();

                        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(new Date());

                        for (Map.Entry<String, List<HourlyItem>> entry : groupedByDay.entrySet()) {

                            String date = entry.getKey();

                            // Bỏ qua hôm nay
                            if (date.equals(today)) continue;

                            List<HourlyItem> dayItems = entry.getValue();

                            float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
                            String dayDesc = dayItems.get(0).getWeather().get(0).getDescription();

                            for (HourlyItem item : dayItems) {
                                float t = item.getMain().getTemp();
                                if (t > max) max = t;
                                if (t < min) min = t;
                            }

                            items.add(new FutureDomains(
                                    formatDate(date), getWeatherPic(dayDesc),
                                    dayDesc, (int) max, (int) min));
                        }

                        adapterTomorrow = new FutureAdapters(items);
                        recyclerView.setAdapter(adapterTomorrow);
                        lastItems = items;
                        updateRecyclerView();
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat input  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH);
            return output.format(input.parse(dateStr));
        } catch (Exception e) { return dateStr; }
    }

    private String getWeatherPic(String desc) {
        desc = desc.toLowerCase();
        if (desc.contains("cloud")) return "cloudy";
        if (desc.contains("rain"))  return "rainy";
        if (desc.contains("clear")) return "sunny";
        if (desc.contains("storm")) return "storm";
        if (desc.contains("wind"))  return "windy";
        return "cloudy";
    }

    private int getWeatherDrawable(String desc) {
        desc = desc.toLowerCase();
        if (desc.contains("rain"))  return R.drawable.rainy;
        if (desc.contains("storm")) return R.drawable.storm;
        if (desc.contains("clear")) return R.drawable.sunny;
        if (desc.contains("wind"))  return R.drawable.windy;
        return R.drawable.cloudy_sunny;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void setVariable() {
        ConstraintLayout backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(v ->
                startActivity(new Intent(FutureActivity.this, MainActivity.class))
        );
        switchUnit = findViewById(R.id.switchUnit);
        switchUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCelsius = !isChecked;
            updateTomorrowTemp();
            updateRecyclerView();
        });
    }
    private void updateTomorrowTemp() {
        if (isCelsius) {
            txtTomorrowTemp.setText(lastAvgTempC + "°C");
        } else {
            int f = Math.round(lastAvgTempC * 9f / 5f + 32f);
            txtTomorrowTemp.setText(f + "°F");
        }
    }

    private void updateRecyclerView() {
        if (lastItems.isEmpty()) return;
        ArrayList<FutureDomains> converted = new ArrayList<>();
        for (FutureDomains d : lastItems) {
            int max = isCelsius ? d.getHighTemp() : Math.round(d.getHighTemp() * 9f / 5f + 32f);
            int min = isCelsius ? d.getLowTemp() : Math.round(d.getLowTemp() * 9f / 5f + 32f);
            converted.add(new FutureDomains(d.getDay(), d.getPicPath(), d.getStatus(), max, min));
        }
        adapterTomorrow = new FutureAdapters(converted);
        recyclerView.setAdapter(adapterTomorrow);
    }
}