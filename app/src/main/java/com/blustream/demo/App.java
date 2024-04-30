package com.blustream.demo;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.bugsnag.android.Bugsnag;

import io.blustream.logger.Log;
import io.blustream.sulley.sensor.DefaultSensorManagerConfig;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Bugsnag.start(this);
        Bugsnag.notify(new RuntimeException("Test error"));
        Log.configure(new DefaultSensorManagerConfig().getLoggerConfig());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.configure(new DefaultSensorManagerConfig().getLoggerConfig());
    }
}
