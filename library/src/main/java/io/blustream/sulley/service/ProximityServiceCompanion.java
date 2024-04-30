package io.blustream.sulley.service;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.blustream.logger.Log;
import io.blustream.sulley.models.ManufacturerData;
import io.blustream.sulley.repository.Repository;
import io.blustream.sulley.repository.RepositoryImpl;
import io.blustream.sulley.repository.data.ProximityLogEvent;
import io.blustream.sulley.sensor.Sensor;
import io.blustream.sulley.sensor.SensorLifecycleListener;
import io.blustream.sulley.sensor.SensorManagerImpl;
import io.blustream.sulley.sensor.VisibilityStateMonitor;

public class ProximityServiceCompanion {

    // Catch this broadcast action on client side to handle.
    private static final String ON_PROXIMITY_STATE_CHANGED_ACTION = "ON_PROXIMITY_STATE_CHANGED_ACTION";
    private static final String ON_PROXIMITY_STATE_CHANGED_INFO = "ON_PROXIMITY_STATE_CHANGED_INFO";
    private static final String START_PROXIMITY_FOR_SERIAL = "START_PROXIMITY_FOR_SERIAL";
    private static final String STOP_PROXIMITY_FOR_SERIAL = "STOP_PROXIMITY_FOR_SERIAL";
    private static final String ACTION_TARGET_SERIAL = "ACTION_TARGET_SERIAL";
    private static MutableLiveData<Boolean> isReceiverRegistered = new MutableLiveData<>(false);
    private Handler handler = new Handler(Looper.myLooper());
    private Runnable reconnectRunnable;
    private SensorManagerImpl mSensorManager;
    private SensorLifecycleListener sensorLifecycleListener;
    private List<VisibilityStateMonitor> visibilityStateMonitors = new ArrayList<>();
    private Date proximityScanStarted, proximityScanFinished;
    private Repository repository;
    private Set<String> serialsToScan = new HashSet<>();
    private BroadcastReceiver receiver;
    private ParentProximityService parentProximityService;
    private boolean isStarted, isInitialized;

    public ProximityServiceCompanion(ParentProximityService parentProximityService) {
        this.parentProximityService = parentProximityService;
    }

    public static Intent getAddSerialIntent(String serial) {
        IntentBuilder intentBuilder = new IntentBuilder();
        intentBuilder.setSerial(serial);
        intentBuilder.setAction(ProximityServiceCompanion.START_PROXIMITY_FOR_SERIAL);
        return intentBuilder.build();
    }

    public static Intent getRemoveSerialIntent(String serial) {
        IntentBuilder intentBuilder = new IntentBuilder();
        intentBuilder.setSerial(serial);
        intentBuilder.setAction(ProximityServiceCompanion.STOP_PROXIMITY_FOR_SERIAL);
        return intentBuilder.build();
    }

    public static LiveData<Boolean> getIsReceiverRegistered() {
        return isReceiverRegistered;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public void onStartCommand(Intent intent) {
        Log.d("onStartCommand" + intent.getAction());
        repository = RepositoryImpl.getRepository(parentProximityService.getApplication());
        initAndRegisterMessageReceiver();
        if (intent.getAction() != null) {
            handleIntent(intent);
        }
    }

    private void handleIntent(Intent intent) {
        Log.d("handleIntent" + intent.getAction());
        String serial = intent.getStringExtra(ACTION_TARGET_SERIAL);
        switch (intent.getAction()) {
            case START_PROXIMITY_FOR_SERIAL:
                serialsToScan.add(serial);
                if (!isInitialized) {
                    initProximityMonitoring();
                }
                restartMonitoring();
                break;
            case STOP_PROXIMITY_FOR_SERIAL:
                serialsToScan.remove(serial);
                restartMonitoring();
                break;
        }
    }

    private void initAndRegisterMessageReceiver() {
        Log.d("initAndRegisterMessageReceiver");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null) {
                    handleIntent(intent);
                } else {
                    Log.e("Intent has no action");
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(START_PROXIMITY_FOR_SERIAL);
        intentFilter.addAction(STOP_PROXIMITY_FOR_SERIAL);
        LocalBroadcastManager.getInstance(parentProximityService.getApplication()).registerReceiver(receiver,
                intentFilter);
        isReceiverRegistered.postValue(true);
    }

    private synchronized void updateSensorVisibilityByBeacon(Sensor sensor) {
        Log.d("updateSensorVisibilityByBeacon. " + sensor);
        Sensor knownSensor = mSensorManager.getSensorCache().getExistingSensorFromSerialNumber(sensor.getSerialNumber());
        if (knownSensor != null) {
            ProximityLogEvent proximityLogEvent = new ProximityLogEvent(knownSensor.getSerialNumber(), new Date(), ProximityLogEvent.Type.IBEACON);
            if (shouldIgnore(proximityLogEvent)) return;
            knownSensor.getVisibilityStatus().setIBeaconAdvertised(true);
            repository.addLog(proximityLogEvent);
        }
    }

    private synchronized void updateSensorVisibilityByAdv(Sensor sensor) {
        Log.d("updateSensorVisibilityByAdv. " + sensor.getBleDevice().getMacAddress());
        Sensor knownSensor = mSensorManager.getSensorCache().getExistingSensorFromBleDevice(sensor.getBleDevice());
        if (knownSensor != null) {
            ProximityLogEvent proximityLogEvent = new ProximityLogEvent(sensor.getSerialNumber(), new Date(), ProximityLogEvent.Type.ADV);
            if (shouldIgnore(proximityLogEvent)) return;
            knownSensor.getVisibilityStatus().setBlustreamAdvertised(true);
            repository.addLog(proximityLogEvent);
        }
    }

    private synchronized boolean shouldIgnore(ProximityLogEvent newEvent) {
        Log.d("shouldIgnore");
        List<ProximityLogEvent> logs = repository.getLogs().getValue();
        if (logs.size() > 0) {
            ProximityLogEvent previousLog = logs.get(logs.size() - 1);
            boolean b = newEvent.getDate().getTime() - previousLog.getDate().getTime() <= 1000;
            Log.d("shouldIgnore = " + b);
            return b;
        }
        return false;
    }

    private void initSensorLifecycleListener() {
        Log.d("initSensorLifecycleListener");
        mSensorManager.setProxLifecycleListener(
                sensorLifecycleListener = new SensorLifecycleListener() {
                    @Override
                    public void sensorDidAdvertise(Sensor sensor, ManufacturerData manufacturerData, int RSSI) {
                        updateSensorVisibilityByAdv(sensor);
                        Log.d("sensorDidAdvertise. " + sensor.getNativeDevice().getAddress());
                    }

                    @Override
                    public void onBeaconReceived(Sensor sensor, int RSSI) {
                        Log.d("onBeaconReceived. " + sensor);
                        updateSensorVisibilityByBeacon(sensor);
                    }

                    @Override
                    public void onProximityStateChanged(Sensor sensor) {
                        Log.d("onProximityStateChanged. " + sensor);
                        //todo change notification message or/and send Broadcast.
                        String log = sensor.getSerialNumber() + " isWithinProximity = " + sensor.isWithinProximity() + "\n " + new Date().toString();
                        // for testing//
                        Log.d(log);
                        parentProximityService.onProximityStateChanged(sensor);
                        Intent intent = new Intent(ON_PROXIMITY_STATE_CHANGED_ACTION);
                        intent.putExtra(ON_PROXIMITY_STATE_CHANGED_INFO, log);
                        LocalBroadcastManager.getInstance(parentProximityService.getApplication()).sendBroadcast(intent);
                    }
                });
    }

    private void initProximityMonitoring() {
        Log.d("initProximityMonitoring");
        SensorManagerImpl.init(parentProximityService.getApplication());
        mSensorManager = SensorManagerImpl.getInstance();
        initSensorLifecycleListener();
        startStateChangeMonitoring();
        reconnectRunnable = () -> {
            Log.d("reconnectRunnable");
            restartMonitoring();
            handler.postDelayed(reconnectRunnable, 120000);
        };
        handler.post(reconnectRunnable);
        repository.setProximityStarted(proximityScanStarted = new Date());
        repository.setIsProximityScanning(true);
        isInitialized = true;
    }

    private void restartMonitoring() {
        Log.d("restartMonitoring");
        mSensorManager.stopProxScanning();
        isStarted = false;
        if (serialsToScan.isEmpty()) {
            Log.d("no macs to monitor");
        } else {
            mSensorManager.startProxScanning(serialsToScan);
            isStarted = true;
        }
    }

    private void startStateChangeMonitoring() {
        Log.d("startStateChangeMonitoring");
        List<Sensor> mSensors = mSensorManager.getSensorCache().getSensors();
        stopStateChangeMonitoring();
        visibilityStateMonitors.clear();
        for (Sensor sensor : mSensors) {
            if (serialsToScan.contains(sensor.getSerialNumber())) {
                VisibilityStateMonitor monitor = new VisibilityStateMonitor(sensor, sensorLifecycleListener);
                monitor.startStateMonitoring();
                visibilityStateMonitors.add(monitor);
            }
        }
    }

    private void stopStateChangeMonitoring() {
        Log.d("stopStateChangeMonitoring");
        for (VisibilityStateMonitor visibilityStateMonitor : visibilityStateMonitors) {
            visibilityStateMonitor.stopStateMonitoring();
        }
        visibilityStateMonitors.clear();
    }

    public void onDestroy() {
        Log.d("onDestroy");
        serialsToScan.clear();
        LocalBroadcastManager.getInstance(parentProximityService.getApplication()).unregisterReceiver(receiver);
        isReceiverRegistered.postValue(false);
        mSensorManager.stopProxScanning();
        mSensorManager.setProxLifecycleListener(null);
        stopStateChangeMonitoring();
        proximityScanFinished = new Date();
        handler.removeCallbacksAndMessages(null);
        repository.setScanDuration(proximityScanFinished.getTime() - proximityScanStarted.getTime());
        repository.setIsProximityScanning(false);
    }

    public Set<String> getSerialsToScan() {
        return serialsToScan;
    }

    public void setSerialsToScan(Set<String> serialsToScan) {
        this.serialsToScan = serialsToScan;
        restartMonitoring();
    }

    public interface ParentProximityService {
        void onProximityStateChanged(Sensor sensor);

        Application getApplication();
    }

    static class IntentBuilder {
        private String action, mac, serial;

        void setAction(String action) {
            this.action = action;
        }

        //void setMac(String mac) {
//            this.mac = mac;
//        }

        void setSerial(String serial) {
            this.serial = serial;
        }

        Intent build() {
            Intent intent = new Intent();
//            if (mac != null) {
//                intent.putExtra(ACTION_TARGET_MAC, mac);
//            }

            if (action != null) {
                intent.setAction(action);
            }

            if (serial != null) {
                intent.putExtra(ACTION_TARGET_SERIAL, serial);
            }
            return intent;
        }
    }
}

