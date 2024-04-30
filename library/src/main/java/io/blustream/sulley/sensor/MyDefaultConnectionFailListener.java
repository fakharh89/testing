package io.blustream.sulley.sensor;

import android.os.Handler;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;

import androidx.annotation.NonNull;
import io.blustream.logger.Log;

class MyDefaultConnectionFailListener implements BleDevice.ConnectionFailListener {
    private final int mRetryCount = 2;
    private final int mFailCountBeforeUsingAutoConnect = 2;
    private final int mReconnectTime = 10;
    private final SensorCache mSensorCache;

    public MyDefaultConnectionFailListener(@NonNull SensorCache sensorCache) {
        mSensorCache = sensorCache;
    }

    @Override
    public Please onEvent(ConnectionFailEvent connectionFailEvent) {
        BleDevice device = connectionFailEvent.device();
        Sensor sensor = mSensorCache.getExistingSensorFromBleDevice(device);

        if (!sensor.shouldReconnect()) {
            return Please.doNotRetry();
        }

        if (!connectionFailEvent.status().allowsRetry()) {
            Log.e(connectionFailEvent.device().getName_override()
                    + " Connect Fail Event - doesn't allow retry - " + connectionFailEvent.status());
            return Please.retry();
        }

        if (connectionFailEvent.device().is(BleDeviceState.RECONNECTING_LONG_TERM)) {
            Log.e(connectionFailEvent.device().getName_override() + " Connect Fail Event - reconnecting long term");
            return Please.retry();
        }

        if (connectionFailEvent.failureCountSoFar() > mRetryCount) {
            Log.w(connectionFailEvent.device().getName_override()
                    + " Giving up connecting - trying again in " + mReconnectTime + " seconds");
            Handler h = new Handler();
            h.postDelayed(() -> {
                if (sensor.shouldReconnect()) {
                    Log.w(sensor.getSerialNumber() + " Connecting to sensor after we failed");
                    sensor.connect();
                }
                else {
                    Log.w(sensor.getSerialNumber() + " Sensor reconnect changed.");
                }
            }, mReconnectTime * 1000);
            return Please.doNotRetry();
        }

        if (connectionFailEvent.failureCountSoFar() >= mFailCountBeforeUsingAutoConnect) {
            return Please.retryWithAutoConnectTrue();
        }
        else {
            if (connectionFailEvent.status() == Status.NATIVE_CONNECTION_FAILED && connectionFailEvent.timing() == Timing.TIMED_OUT) {
                if (connectionFailEvent.autoConnectUsage() == AutoConnectUsage.USED) {
                    return Please.retryWithAutoConnectFalse();
                }
                else if (connectionFailEvent.autoConnectUsage() == AutoConnectUsage.NOT_USED) {
                    return Please.retryWithAutoConnectTrue();
                }
                else {
                    return Please.retry();
                }
            }
            else {
                return Please.retry();
            }
        }
    }
}
