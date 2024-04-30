package io.blustream.sulley.sensor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import io.blustream.logger.Log;

public class ConnectTask extends BroadcastReceiver {

    private static final int REQUEST_CODE = 0;
    private static final int REPEAT_INTERVAL_DEFAULT_MILLIS = 30_000;
    private static SensorCache mSensorCache;

    private int mInterval = REPEAT_INTERVAL_DEFAULT_MILLIS;

    public ConnectTask() {
        mSensorCache = ConnectTask.getSensorCache();
    }

    public ConnectTask(SensorCache sensorCache) {
        mSensorCache = sensorCache;
    }

    public static SensorCache getSensorCache() {
        return mSensorCache;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("onReceive", "Connect task running");
        if (mSensorCache != null) {
            for (Sensor sensor : mSensorCache.getSensors()) {
                if (sensor.shouldReconnect()) {
                    sensor.connect();
                } else {
                    Log.i("Skipping sensor " + sensor.getSerialNumber());
                }
            }
        }
        start(context);
    }

    public void start(Context context) {
        Intent intent = new Intent(context, ConnectTask.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mInterval, pendingIntent);
        } else {
            Log.i("Alarm Manager is null");
        }
    }

    public void restartWithInterval(Context context, int interval) {
        mInterval = interval;
        cancel(context);
        start(context);
    }

    public void cancel(Context context) {
        Intent intent = new Intent(context, ConnectTask.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(sender);
        } else {
            Log.i("Alarm Manager is null");
        }
    }
}
