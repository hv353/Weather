// File: com/example/weather/Helper/WeatherNotificationHelper.java
package com.example.weather.Helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.weather.R;

public class WeatherNotificationHelper {

    private static final String CHANNEL_ID = "weather_alert";
    private static final String CHANNEL_NAME = "Cảnh báo thời tiết";
    private static final int NOTIF_ID = 1001;

    private final Context context;
    private final NotificationManager manager;

    public WeatherNotificationHelper(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo khi thời tiết xấu");
            manager.createNotificationChannel(channel);
        }
    }

    public void notifyIfBadWeather(String description) {
        String desc = description.toLowerCase();
        String title = null;
        String message = null;

        if (desc.contains("storm") || desc.contains("thunderstorm")) {
            title = "⚡ Cảnh báo: Giông bão!";
            message = "Hạn chế ra ngoài, tránh vùng trống và cây cao.";
        } else if (desc.contains("heavy rain") || desc.contains("mưa lớn")) {
            title = "🌧️ Cảnh báo: Mưa lớn!";
            message = "Có thể ngập úng, hãy cẩn thận khi di chuyển.";
        } else if (desc.contains("rain")) {
            title = "🌦️ Thời tiết: Có mưa";
            message = "Mang theo ô khi ra ngoài.";
        } else if (desc.contains("snow")) {
            title = "❄️ Cảnh báo: Tuyết rơi!";
            message = "Đường trơn, hãy đi cẩn thận.";
        } else if (desc.contains("fog") || desc.contains("mist")) {
            title = "🌫️ Cảnh báo: Sương mù!";
            message = "Tầm nhìn hạn chế, lái xe chậm và bật đèn.";
        } else if (desc.contains("wind") || desc.contains("gale")) {
            title = "💨 Cảnh báo: Gió mạnh!";
            message = "Cẩn thận vật bay, hạn chế ra ngoài.";
        }

        if (title != null) {
            showNotification(title, message);
        }
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.cloudy) // dùng icon drawable sẵn có
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(NOTIF_ID, builder.build());
    }
}