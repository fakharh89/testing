package io.blustream.sulley.sensor;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;

public enum SensorConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTED;

    static SensorConnectionState getState(BleDevice device) {
        if (device.is(BleDeviceState.INITIALIZED)) {
            return SensorConnectionState.CONNECTED;
        }

        if (device.is(BleDeviceState.CONNECTING_OVERALL)) {
            return SensorConnectionState.CONNECTING;
        }

        if (device.is(BleDeviceState.DISCONNECTED)) {
            return SensorConnectionState.DISCONNECTED;
        }

        return SensorConnectionState.DISCONNECTED;
    }
}
