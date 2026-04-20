package com.example.weather.Activitis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.example.weather.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String API_KEY = "37c6ba63ded1fc4c85bbb65eb2846d0d";

    // Các layer OpenWeatherMap hỗ trợ
    private static final String LAYER_CLOUDS = "clouds_new";
    private static final String LAYER_TEMP   = "temp_new";
    private static final String LAYER_RAIN   = "precipitation_new";

    private GoogleMap googleMap;
    private TileOverlay currentOverlay;
    private double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Nhận tọa độ từ MainActivity
        lat = getIntent().getDoubleExtra("lat", 21.0285);
        lon = getIntent().getDoubleExtra("lon", 105.8542);

        // Khởi tạo bản đồ
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // Gắn sự kiện các nút
        Button btnClouds = findViewById(R.id.btnClouds);
        Button btnTemp   = findViewById(R.id.btnTemp);
        Button btnRain   = findViewById(R.id.btnRain);

        btnClouds.setOnClickListener(v -> setWeatherLayer(LAYER_CLOUDS));
        btnTemp  .setOnClickListener(v -> setWeatherLayer(LAYER_TEMP));
        btnRain  .setOnClickListener(v -> setWeatherLayer(LAYER_RAIN));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;


        LatLng position = new LatLng(lat, lon);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 7f));


        setWeatherLayer(LAYER_CLOUDS);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Vị trí của bạn")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private void setWeatherLayer(String layerName) {
        if (currentOverlay != null) {
            currentOverlay.remove();
        }

        // Tạo TileProvider từ OpenWeatherMap
        UrlTileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String url = String.format(Locale.US,
                        "https://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=%s",
                        layerName, zoom, x, y, API_KEY);
                try {
                    return new URL(url);
                } catch (MalformedURLException e) {
                    return null;
                }
            }
        };

        // Thêm overlay lên bản đồ
        currentOverlay = googleMap.addTileOverlay(
                new TileOverlayOptions()
                        .tileProvider(tileProvider)
                        .transparency(0.2f)  // 0 = đục, 1 = trong suốt
        );
    }
}