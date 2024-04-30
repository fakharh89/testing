package io.blustream.sulley.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Date;
import java.util.Random;

import io.blustream.logger.Log;
import io.blustream.sulley.sensor.Sensor;
import io.blustream.sulley.utilities.NotificationUtils;

public class ForegroundProximityService extends Service implements ProximityServiceCompanion.ParentProximityService {

    public static final String CHANNEL_ID = "ProximityForegroundServiceChannel";
    private static final String NOTIFICATION_ICON = "NOTIFICATION_ICON";

    private ProximityServiceCompanion proximityServiceCompanion;

    public static Intent getStartIntent(Context context, @DrawableRes int notificationIconRes) {
        IntentBuilder intentBuilder = new IntentBuilder();
        intentBuilder.setNotificationIconResId(notificationIconRes);
        return intentBuilder.build(context);
    }

    public static Intent getStopIntent(Context context) {
        IntentBuilder intentBuilder = new IntentBuilder();
        return intentBuilder.build(context);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d("onStartCommand");
        Notification notification = createStarterNotification();
        startForeground(1, notification);

        proximityServiceCompanion.onStartCommand(intent);
        createNotificationChannel();
        return START_REDELIVER_INTENT;
    }

    private void sendNotification(String message) {
        Log.d("sendNotification = " + message);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int mNotificationId = new Random(new Date().getTime()).nextInt(10_000);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Sulley")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(mNotificationId, builder.build());
    }


    private Notification createStarterNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationUtils.createNotificationChannel(notificationManager, NotificationUtils.CHANNEL_ID);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID);
        builder.setContentTitle("Monitoring proximity of your sensors.");
        return builder.build();
    }

    private void createNotificationChannel() {
        Log.d("createNotificationChannel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    @Override
    public void onDestroy() {
        Log.d("onDestroy");
        proximityServiceCompanion.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("onConfigurationChanged");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        proximityServiceCompanion = new ProximityServiceCompanion(this);
        Log.d("onCreate");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onLowMemory() {
        Log.d("onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d("onTrimMemory");
        super.onTrimMemory(level);
    }

    @Override
    public String getOpPackageName() {
        Log.d("getOpPackageName");
        return super.getOpPackageName();
    }

    @Override
    public void onProximityStateChanged(Sensor sensor) {
        String message = sensor.getSerialNumber() + " isWithinProximity = " + sensor.isWithinProximity() + " " + new Date().toString();
        Log.d(message);
        sendNotification(message);
    }

    static class IntentBuilder {

        private int notificationIconResId;

        void setNotificationIconResId(int notificationIconResId) {
            this.notificationIconResId = notificationIconResId;
        }

        Intent build(Context context) {
            Intent intent = new Intent(context, ForegroundProximityService.class);
            if (notificationIconResId != 0) {
                intent.putExtra(NOTIFICATION_ICON, notificationIconResId);
            }
            return intent;
        }
    }
}
