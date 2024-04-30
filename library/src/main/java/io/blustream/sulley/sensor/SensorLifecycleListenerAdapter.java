package io.blustream.sulley.sensor;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.DeviceStateListener;

import io.blustream.logger.Log;

public class SensorLifecycleListenerAdapter implements DeviceStateListener {
    private final Sensor mSensor;
    private final SensorCache mSensorCache;
    private SensorLifecycleListener lifecycleListener;
    private SensorLifecycleListener proxListener;
    SensorLifecycleListenerAdapter(@NonNull SensorCache cache) {
        this(null, cache, null);
    }

    SensorLifecycleListenerAdapter(@NonNull Sensor sensor, SensorLifecycleListener lifecycleListener) {
        this(sensor, null, lifecycleListener);
    }

    SensorLifecycleListenerAdapter(@NonNull SensorCache cache,
                                   SensorLifecycleListener listener) {
        this(null, cache, listener);
    }

    private SensorLifecycleListenerAdapter(Sensor sensor, SensorCache cache,
                                           SensorLifecycleListener lifecycleListener) {
        if (sensor == null && cache == null) {
            throw new IllegalArgumentException("Sensor and cache cannot be null!");
        }
        mSensor = sensor;
        mSensorCache = cache;
        this.lifecycleListener = lifecycleListener;
    }

    public SensorLifecycleListener getLifecycleListener() {
        return lifecycleListener;
    }

    public void setLifecycleListener(SensorLifecycleListener lifecycleListener) {
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public void onEvent(BleDevice.StateListener.StateEvent stateEvent) {
        if (lifecycleListener == null && proxListener == null) {
            Log.e("No listeners set up");
            return;
        }

        Sensor sensor = getSensor(stateEvent.device());

        if (sensor == null) {
            Log.e("No sensor");
            return;
        }

        Log.consoleInfo(sensor.getSerialNumber() + " State: " + stateEvent.toString());

        /* These states are hella confusing.  Use stateEvent.toString() to read this.  State events
        track the old and the new  state.  didExit/didEnter are helper functions to look for the
        difference between the old and new states.

        Example:
        device     = <no_name>_AF9E
        entered    = [CONNECTING]
        exited     = [DISCONNECTED]
        current    = [UNBONDED, CONNECTING]
        gattStatus = GATT_SUCCESS(0)

        entered: shows any new states added
        exited: shows any states that were left
        current: shows current states

        This particular event shows that a sensor has started the connection process, but is not
        yet connected.  It occurred after the first connection request.
        */

        if (stateEvent.didExit(BleDeviceState.RECONNECTING_SHORT_TERM)
                && !stateEvent.didEnter(BleDeviceState.DISCONNECTED)) {
            if (lifecycleListener != null) {
                lifecycleListener.sensorDidReconnect(sensor);
            } else if (proxListener != null) {
                proxListener.sensorDidReconnect(sensor);
            }
        } else if (stateEvent.didEnter(BleDeviceState.RECONNECTING_SHORT_TERM)) {
            if (lifecycleListener != null) {
                lifecycleListener.sensorDidTemporarilyDisconnect(sensor);
            } else if (proxListener != null) {
                proxListener.sensorDidTemporarilyDisconnect(sensor);
            }
        }

        if (stateEvent.didEnter(BleDeviceState.INITIALIZED)) {
            if (lifecycleListener != null) {
                lifecycleListener.sensorDidConnect(sensor);
            } else if (proxListener != null) {
                proxListener.sensorDidConnect(sensor);
            }

        } else if (stateEvent.didExit(BleDeviceState.CONNECTED)) {
            if (lifecycleListener != null) {
                lifecycleListener.sensorDidDisconnect(sensor);
            } else if (proxListener != null) {
                proxListener.sensorDidDisconnect(sensor);
            }
        }
    }

    private Sensor getSensor(BleDevice device) {
        if (mSensor != null) {
            return mSensor;
        }

        Sensor sensor = mSensorCache.getExistingSensorFromBleDevice(device);
//
//        if (sensor == null) {
//            // TODO handle this more elegantly.
//            throw new IllegalStateException("Sensor doesn't exist but we're connected!");
//        }

        return sensor;
    }

    public SensorLifecycleListener getProxListener() {
        return proxListener;
    }

    public void setProxListener(SensorLifecycleListener proxListener) {
        this.proxListener = proxListener;
    }
}
