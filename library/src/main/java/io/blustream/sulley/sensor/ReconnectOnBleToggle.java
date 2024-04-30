package io.blustream.sulley.sensor;

import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerState;
import com.idevicesinc.sweetblue.ManagerStateListener;

import io.blustream.logger.Log;

class ReconnectOnBleToggle implements ManagerStateListener {
    private final SensorCache mSensorCache;

    public ReconnectOnBleToggle(SensorCache sensorCache) {
        mSensorCache = sensorCache;
    }

    @Override
    public void onEvent(BleManager.StateListener.StateEvent stateEvent) {
        if (!stateEvent.didEnter(BleManagerState.IDLE)) {
            Log.i("BleManager State Event: " + stateEvent);
        }

        if (stateEvent.didEnter(BleManagerState.ON)) {
            connectToAllReconnectingSensors();
        }
    }

    private void connectToAllReconnectingSensors() {
        for (Sensor sensor : mSensorCache.getSensors()) {
            if (sensor.shouldReconnect()) {
                sensor.connect();
            } else {
                Log.i("Not connecting to sensor " + sensor.getSerialNumber());
            }
        }
    }
}
