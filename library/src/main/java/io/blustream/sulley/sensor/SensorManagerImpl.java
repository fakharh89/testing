package io.blustream.sulley.sensor;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.BleManagerState;

import java.util.Set;

import io.blustream.logger.Log;
import io.blustream.sulley.UhOhRemedyListenerAdapter;
import io.blustream.sulley.models.ManufacturerData;
import io.blustream.sulley.utilities.MacAddressHelper;
import io.blustream.sulley.utilities.PermissionChecker;

/**
 * SensorManagerImpl doc!
 */
public class SensorManagerImpl implements SensorManager {

    private static final int FOREGROUND_RECONNECTION_INTERVAL_MILLIS = 30_000;
    private static final int BACKGROUND_RECONNECTION_INTERVAL_MILLIS = 120_000;
    private static SensorManagerImpl instance;
    private final BleManager mBleManager;
    private final SensorCache mSensorCache;
    @Nullable
    private UhOhRemedyListener mUhOhRemedyListener;
    private AlternativeScanner mAlternativeScanner;
    private ConnectTask connectTask;
    private boolean stopByBLEOff;
    private Context context;
    private SensorLifecycleListenerAdapter sensorLifecycleListenerAdapter;

    /**
     * Initialize
     *
     * @param context app context
     * @param config  config class
     */
    private SensorManagerImpl(Context context, SensorManagerConfig config, String licenseKey, @Nullable StateProvider stateProvider) {
        this.context = context;
        if (config.getLoggerConfig() != null) {
            Log.configure(config.getLoggerConfig());
        }

        BleManagerConfig bleManagerConfig = new SweetBlueConfig();
        mBleManager = BleManager.get(context, bleManagerConfig);
        mSensorCache = new SensorCacheImpl();

        mAlternativeScanner = new AlternativeScanner(context, mBleManager, mSensorCache, config.getCompatibleSensorIdentifiers());

        // This time is in SensorManager because we want these events to trigger all at the same time
        startTimer();

        setupStandardListeners();
        if (stateProvider != null) {
            registerLifecycleCallback(stateProvider);
        }
    }

    public static void init(Context context, SensorManagerConfig config, String licenseKey, @Nullable StateProvider stateProvider) {
        if (instance == null)
            instance = new SensorManagerImpl(context, config, licenseKey, stateProvider);
    }

    public static void init(Context context, SensorManagerConfig config) {
        init(context, config, null, null);
    }

    public static void init(Context context) {
        init(context, new DefaultSensorManagerConfig(), null, null);
    }

    @NonNull
    public static SensorManagerImpl getInstance() {
        if (instance == null) {
            throw new NullPointerException("SensorManager is not initialized. Call init(args...) before");
        }
        return instance;
    }

    @Override
    public SensorCache getSensorCache() {
        return mSensorCache;
    }

    @Override
    @Nullable
    public UhOhRemedyListener getUhOhRemedyListener() {
        return mUhOhRemedyListener;
    }

    @Override
    public void setUhOhRemedyListener(@Nullable UhOhRemedyListener uhOhRemedyListener) {
        this.mUhOhRemedyListener = uhOhRemedyListener;
        mBleManager.setListener_UhOh(new UhOhRemedyListenerAdapter(uhOhRemedyListener));
    }

    @Override
    public SensorLifecycleListener getAdvLifecycleListener() {
        return sensorLifecycleListenerAdapter.getLifecycleListener();
    }

    @Override
    public void setLifecycleListener(SensorLifecycleListener lifecyclelistener) {
        //this.advListener = advlistener;
        if (sensorLifecycleListenerAdapter == null) {
            mBleManager.setListener_DeviceState(sensorLifecycleListenerAdapter =
                    new SensorLifecycleListenerAdapter(mSensorCache, lifecyclelistener));
        } else {
            sensorLifecycleListenerAdapter.setLifecycleListener(lifecyclelistener);
        }
    }

    @Override
    public boolean startAdvScanning() {
        stopIfStarted();
        boolean isStarted = mAlternativeScanner.startAdv(new AlternativeScannerListener() {
            @Override
            public void sensorDidAdvertise(Sensor sensor, ManufacturerData manufacturerData, int RSSI) {
                sensorLifecycleListenerAdapter.getLifecycleListener().sensorDidAdvertise(sensor, manufacturerData, RSSI);
            }

            @Override
            public void onBeaconReceived(Sensor sensor, int RSSI) {
                sensorLifecycleListenerAdapter.getLifecycleListener().onBeaconReceived(sensor, RSSI);
            }
        });
        logStarted(isStarted);
        return isStarted;
    }

    @Override
    public boolean startProxScanning(Set<String> serials) {
        stopIfStarted();
        boolean isStarted = mAlternativeScanner.startProximityMonitoring(new AlternativeScannerListener() {
            @Override
            public void sensorDidAdvertise(Sensor sensor, ManufacturerData manufacturerData, int RSSI) {
                sensorLifecycleListenerAdapter.getProxListener().sensorDidAdvertise(sensor, manufacturerData, RSSI);
            }

            @Override
            public void onBeaconReceived(Sensor sensor, int RSSI) {
                sensorLifecycleListenerAdapter.getProxListener().onBeaconReceived(sensor, RSSI);
            }
        }, serials);
        logStarted(isStarted);
        return isStarted;
    }

    @Override
    public void stopAdvScanning() {
        Log.i("stopAdvScanning");
        mAlternativeScanner.stopAdv();
    }

    @Override
    public void stopProxScanning() {
        Log.i("stopProxScanning");
        mAlternativeScanner.stopProximity();
    }

    @Override
    public boolean isAdvScanning() {
        return mAlternativeScanner.isAdvScanning();
    }

    @Override
    public boolean isProxScanning() {
        return mAlternativeScanner.isProximityScanning();
    }

    public void setProxLifecycleListener(SensorLifecycleListener proxListener) {
        if (sensorLifecycleListenerAdapter == null) {
            sensorLifecycleListenerAdapter = new SensorLifecycleListenerAdapter(mSensorCache);
            sensorLifecycleListenerAdapter.setProxListener(proxListener);
            mBleManager.setListener_DeviceState(sensorLifecycleListenerAdapter);
        } else {
            sensorLifecycleListenerAdapter.setProxListener(proxListener);
        }
    }

    @Override
    @NonNull
    public Sensor getSensorFromSerialNumber(String serialNumber) {
        Sensor sensor = mSensorCache.getExistingSensorFromSerialNumber(serialNumber);
        if (sensor == null) {
            MacAddressHelper helper = new MacAddressHelper();
            String macAddress = helper.getMacFromSerialNumber(serialNumber);

            BleDevice device = mBleManager.newDevice(macAddress);
            device.setName(serialNumber);
            sensor = new SensorImpl(serialNumber, device);
            mSensorCache.addSensor(sensor);
        }
        return sensor;
    }

    @Override
    public void setupPermissions(Activity activity, SetupPermissionsCallback callback) {
        if (callback == null) {
            return;
        }
        PermissionChecker permissionChecker = new PermissionChecker(activity);
        if (!permissionChecker.checkPermissions()) {
            callback.onFailure();
        } else {
            callback.onSuccess();
        }
    }

    private void registerLifecycleCallback(StateProvider stateProvider) {
        stateProvider.addListener(new StateProvider.Listener() {
            @Override
            public void toForegroundState() {
                connectTask.restartWithInterval(context, FOREGROUND_RECONNECTION_INTERVAL_MILLIS);
            }

            @Override
            public void toBackgroundState() {
                connectTask.restartWithInterval(context, BACKGROUND_RECONNECTION_INTERVAL_MILLIS);
            }
        });
    }

    private void logStarted(boolean isStarted) {
        if (isStarted) {
            Log.i("Started scanning");
        } else {
            Log.e("Failed to start scanning!");
        }
    }

    private void stopIfStarted() {
        if (mAlternativeScanner.isAdvScanning()) {
            mAlternativeScanner.stopScan();
        }
    }

    private void setupStandardListeners() {
        mBleManager.setListener_UhOh(uhOhEvent -> Log.e("UhOh " + uhOhEvent.toString()));
        mBleManager.setListener_State(new ReconnectOnBleToggle(mSensorCache) {
            @Override
            public void onEvent(BleManager.StateListener.StateEvent stateEvent) {
                super.onEvent(stateEvent);
                if (stateEvent.didEnter(BleManagerState.ON)) {
                    if (stopByBLEOff) {
                        startTimer();
                        stopByBLEOff = false;
                    }
                }
                if (stateEvent.didEnter(BleManagerState.OFF)) {
                    connectTask.cancel(context);
                    stopByBLEOff = true;
                }
            }
        });
        mBleManager.setListener_ConnectionFail(new SweetBlueDefaultConnectionFailListener(4, 3));
    }

    private void startTimer() {
        connectTask = new ConnectTask(mSensorCache);
        connectTask.start(context);
    }
}
