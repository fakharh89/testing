package io.blustream.sulley.sensor;

import android.os.Handler;
import android.os.Looper;

import java.util.Date;

import io.blustream.logger.Log;

public class VisibilityStateMonitor {

    private boolean isMonitoring;
    private Sensor sensor;
    private Handler handler = new Handler(Looper.myLooper());
    private Boolean isWithinProximity;
    private SensorLifecycleListener sensorLifecycleListener;

    public VisibilityStateMonitor(Sensor sensor, SensorLifecycleListener sensorLifecycleListener) {
        this.sensor = sensor;
        this.sensorLifecycleListener = sensorLifecycleListener;
    }

    private long whenNextCheck() {
        long result = sensor.getVisibilityStatus().getDate().getTime() +
                sensor.getVisibilityStatus().getEventTimeout() - new Date().getTime();
        Log.d("whenNextCheck " + result + " sensor = " + sensor.getSerialNumber());
        return Math.max(1000, result);
    }

    public void startStateMonitoring() {
        Log.d("startStateMonitoring");
        if (isMonitoring) {
            return;
        }
        isMonitoring = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isWithinProximity != null && isWithinProximity != sensor.isWithinProximity()) {
                    sensorLifecycleListener.onProximityStateChanged(sensor);
                }
                isWithinProximity = sensor.isWithinProximity();
                handler.postDelayed(this, whenNextCheck());
            }
        }, whenNextCheck());
    }

    public void stopStateMonitoring() {
        Log.d("stopStateMonitoring");
        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
    }
}
