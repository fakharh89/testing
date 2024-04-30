package io.blustream.sulley.sensor;

import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.BleScanPower;
import com.idevicesinc.sweetblue.utils.Interval;

import io.blustream.logger.Log;

// TODO Keep this inside Sulley and don't make public
class SweetBlueConfig extends BleManagerConfig {
    public SweetBlueConfig() {
        super();

//        reconnectFilter = new MyDefaultReconnectFilter();
        reconnectFilter = new SweetBlueDefaultReconnectFilter(Interval.secs(1.0), Interval.secs(10.0),
                Interval.FIVE_SECS, Interval.INFINITE);

        bondingFailFailsConnection = false;

        autoEnableNotifiesOnReconnect = false;

        stopScanOnPause = false;
        autoScanDuringOta = true;
        scanPower = BleScanPower.HIGH_POWER;
        postCallbacksToMainThread = false;

        undiscoverDeviceWhenBleTurnsOff = false;

        autoReconnectDeviceWhenBleTurnsBackOn = false;
        connectFailRetryConnectingOverall = true;

        loggingEnabled = true;
        logger = (i, s, s1) -> {
            String message = s + s1;
            switch (i) {
                case android.util.Log.VERBOSE:
                    Log.i(message);
                    break;
                case android.util.Log.DEBUG:
                    Log.d(message);
                    break;
                case android.util.Log.INFO:
                    Log.i(message);
                    break;
                case android.util.Log.WARN:
                    Log.w(message);
                    break;
                case android.util.Log.ERROR:
                    Log.e(message);
                    break;
                case android.util.Log.ASSERT:
                    Log.e(message);
                    break;
                default:
                    Log.i(message);
                    break;
            }
        };

        // These values are experimental and arbitrary.
//        serviceDiscoveryDelay = Interval.millis(200);
//        useGattRefresh = true;
//        uuidNameMaps = null;
    }
}
