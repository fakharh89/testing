package io.blustream.sulley.sensor;

import io.blustream.sulley.models.ManufacturerData;

interface AlternativeScannerListener {

    void sensorDidAdvertise(Sensor sensor, ManufacturerData manufacturerData, int RSSI);

    void onBeaconReceived(Sensor sensor, int RSSI);
    default void otauSensorDidAdvertise(Sensor sensor, int RSSI) { }

}
