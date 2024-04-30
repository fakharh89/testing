package io.blustream.sulley.sensor;

import com.idevicesinc.sweetblue.BleDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SensorCacheImpl implements SensorCache {
    private final HashMap<String, Sensor> mSensorMap = new HashMap<>();

    @Override
    public Sensor getExistingSensorFromSerialNumber(String serialNumber) {
        return mSensorMap.get(serialNumber);
    }

    @Override
    public Sensor getExistingSensorFromBleDevice(BleDevice device) {
        for (Sensor sensor : mSensorMap.values()) {
            if (sensor.getBleDevice() == device) {
                return sensor;
            }
        }
        return null;
    }

    @Override
    public void addSensor(Sensor sensor) {
        mSensorMap.put(sensor.getSerialNumber(), sensor);
    }

    @Override
    public List<Sensor> getSensors() {
        return new ArrayList<>(mSensorMap.values());
    }
}
