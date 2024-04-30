package io.blustream.sulley.sensor;

import io.blustream.sulley.models.ManufacturerData;

// TODO Make sure these are all set up in the SensorManager and the Sensor
// TODO Add documentation
public interface SensorLifecycleListener {
    default void sensorDidAdvertise(Sensor sensor, ManufacturerData manufacturerData, int RSSI) {
    }

    default void sensorDidConnect(Sensor sensor) {
    }

    default void sensorDidFailToConnect(Sensor sensor) {   // TODO Add error
    }

    default void sensorDidDisconnect(Sensor sensor) {
    }

    default void sensorDidFailToDisconnect(Sensor sensor) {  // TODO Add error
    }

    default void sensorDidTemporarilyDisconnect(Sensor sensor) {
    }

    default void sensorDidReconnect(Sensor sensor) {
    }

    default void onBeaconReceived(Sensor sensor, int RSSI) {
    }

    default void onProximityStateChanged(Sensor sensor) {
    }

}
