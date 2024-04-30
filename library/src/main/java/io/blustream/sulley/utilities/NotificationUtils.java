package io.blustream.sulley.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.Nullable;

public class NotificationUtils {

    public static final String CHANNEL_ID = "foreground_channel_id";

    private NotificationUtils() {}

    public static void createNotificationChannel(@Nullable NotificationManager notificationManager, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
