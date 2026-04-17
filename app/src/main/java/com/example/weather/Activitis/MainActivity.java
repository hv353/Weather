package com.example.weather.Activitis;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.weather.Adapters.HourlyAdapters;
import com.example.weather.Api.WeatherApi;
import com.example.weather.Domains.Hourly;
import com.example.weather.Helper.WeatherNotificationHelper;
import com.example.weather.Model.ForecastResponse;
import com.example.weather.Model.HourlyItem;
import com.example.weather.Model.WeatherResponse;
import com.example.weather.R;
import com.example.weather.Retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapterHourly;
    private TextView txtTemp, txtDesc, txtHumidity, txtWind, txtCity;
    private ImageView imgWeather;
    private Button btnLocation;
    private SwipeRefreshLayout swipeRefresh; // ← thêm mới

    private static final String API_KEY = "37c6ba63ded1fc4c85bbb65eb2846d0d";

    private double currentLat = 0;
    private double currentLon = 0;
    private WeatherNotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTemp      = findViewById(R.id.txtTemp);
        txtDesc      = findViewById(R.id.txtDesc);
        txtHumidity  = findViewById(R.id.txtHumidity);
        txtWind      = findViewById(R.id.txtWind);
        txtCity      = findViewById(R.id.txtCity);
        imgWeather   = findViewById(R.id.imgWeather);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        notificationHelper = new WeatherNotificationHelper(this);

        recyclerView = findViewById(R.id.view1);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        // ── Màu loading indicator ────────────────────────────────────────
        swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light
        );

        // ── Kéo xuống → reload vị trí + thời tiết ───────────────────────
        swipeRefresh.setOnRefreshListener(() -> fetchLocationAndLoad());
        setVariable();
        initLocation();
    }

    // ── Các nút bấm ─────────────────────────────────────────────────────────
    private void setVariable() {
        TextView nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FutureActivity.class);
            intent.putExtra("lat", currentLat);
            intent.putExtra("lon", currentLon);
            startActivity(intent);
        });
        //test thông báo khi có thời tiết xấu
        if (true) {
            String[] testCases = {
                    "thunderstorm", "heavy rain", "rain",
                    "snow", "fog", "wind", "clear sky"
            };
            final int[] index = {0};

            // Thêm Button vào layout, hoặc dùng long-click vào txtCity để test
            txtCity.setOnLongClickListener(v -> {
                String weather = testCases[index[0] % testCases.length];
                notificationHelper.notifyIfBadWeather(weather);
                Toast.makeText(this, "Test: " + weather, Toast.LENGTH_SHORT).show();
                index[0]++;
                return true;
            });
        }

    }

    // ── Xử lý location ──────────────────────────────────────────────────────
    private void initLocation() {
        if (LocationHelper.hasPermission(this)) {
            fetchLocationAndLoad();
        } else {
            LocationHelper.requestPermission(this);
        }
    }

    private void fetchLocationAndLoad() {
        LocationHelper.getCurrentLocation(this, new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReady(double lat, double lon) {
                currentLat = lat;
                currentLon = lon;
                loadCurrentWeather();
                loadHourlyForecast();
                stopRefreshing();
            }

            @Override
            public void onLocationFailed(String reason) {
                Toast.makeText(MainActivity.this,
                        "Không lấy được vị trí, dùng Hà Nội mặc định", Toast.LENGTH_SHORT).show();
                currentLat = 21.0285;
                currentLon = 105.8542;
                loadCurrentWeather();
                loadHourlyForecast();
                stopRefreshing();
            }
        });
    }

    // ── Dừng cả 2 loading indicator ─────────────────────────────────────────
    private void stopRefreshing() {
        runOnUiThread(() -> {
            // Dừng vòng xoay SwipeRefresh
            swipeRefresh.setRefreshing(false);

        });
    }


    // ── Thời tiết hiện tại (theo tọa độ) ────────────────────────────────────
    private void loadCurrentWeather() {
        WeatherApi api = RetrofitClient.getInstance().getClient().create(WeatherApi.class);

        api.getCurrentWeatherByCoords(currentLat, currentLon, API_KEY, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call,
                                           Response<WeatherResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) return;

                        WeatherResponse data = response.body();
                        String desc     = data.getWeather().get(0).getDescription();
                        float  temp     = data.getMain().getTemp();
                        int    humidity = data.getMain().getHumidity();
                        double wind     = data.getWind().getSpeed();
                        String city     = data.getName();

                        txtTemp    .setText(temp + "°C");
                        txtDesc    .setText(desc);
                        txtHumidity.setText(humidity + "%");
                        txtWind    .setText(wind + " m/s");
                        txtCity    .setText(city);
                        notificationHelper.notifyIfBadWeather(desc);

                        int resId = getResources().getIdentifier(
                                getWeatherPic(desc), "drawable", getPackageName());
                        imgWeather.setImageResource(resId);
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        t.printStackTrace();
                        runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                    }
                });
    }

    // ── Dự báo các giờ tới (theo tọa độ) ────────────────────────────────────
    private void loadHourlyForecast() {
        WeatherApi api = RetrofitClient.getInstance().getClient().create(WeatherApi.class);

        api.getHourlyForecastByCoords(currentLat, currentLon, API_KEY, "metric", 8)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call,
                                           Response<ForecastResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) return;

                        List<HourlyItem> forecastList = response.body().getList();
                        ArrayList<Hourly> items = new ArrayList<>();

                        for (HourlyItem item : forecastList) {
                            String time    = item.getDt_txt().substring(11, 16);
                            float  temp    = item.getMain().getTemp();
                            String desc    = item.getWeather().get(0).getDescription();
                            String picPath = getWeatherPic(desc);
                            items.add(new Hourly(time, (int) temp, picPath));
                        }

                        adapterHourly = new HourlyAdapters(items);
                        recyclerView.setAdapter(adapterHourly);
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }

    // ── Icon theo mô tả thời tiết ────────────────────────────────────────────
    private String getWeatherPic(String desc) {
        desc = desc.toLowerCase();
        if (desc.contains("cloud")) return "cloudy";
        if (desc.contains("rain"))  return "rainy";
        if (desc.contains("clear")) return "sunny";
        if (desc.contains("storm")) return "storm";
        if (desc.contains("wind"))  return "windy";
        return "cloudy";
    }
}