package io.blustream.sulley.sensor;

import com.idevicesinc.sweetblue.BleDevice;

import java.util.List;

public interface SensorCache {
    Sensor getExistingSensorFromSerialNumber(String serialNumber);

    Sensor getExistingSensorFromBleDevice(BleDevice device);

    void addSensor(Sensor sensor);

    List<Sensor> getSensors();
}
