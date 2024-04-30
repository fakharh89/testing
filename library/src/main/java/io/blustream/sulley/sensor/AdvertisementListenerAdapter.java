package io.blustream.sulley.sensor;

import com.idevicesinc.sweetblue.BleManager;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.BlustreamManufacturerDataMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.BlustreamManufacturerData;
import io.blustream.sulley.utilities.MacAddressHelper;
import io.blustream.sulley.utilities.SerialNumberHelper;

class AdvertisementListenerAdapter implements BleManager.DiscoveryListener {
    private final SensorCache mSensorCache;
    private final SensorLifecycleListener mListener;
    private final String[] mCompatibleIds;

    AdvertisementListenerAdapter(String[] compatibleIds, SensorCache sensorCache, SensorLifecycleListener listener) {
        mCompatibleIds = compatibleIds;
        mSensorCache = sensorCache;
        mListener = listener;
    }

    @Override
    public void onEvent(BleManager.DiscoveryListener.DiscoveryEvent discoveryEvent) {
        MacAddressHelper macAddressHelper = new MacAddressHelper();

        if (!macAddressHelper.isBlustreamMacAddress(discoveryEvent.macAddress())) {
            return;
        }

        BlustreamManufacturerData manufacturerData = null;
        BlustreamManufacturerDataMapper mapper = new BlustreamManufacturerDataMapper();

        try {
            manufacturerData = mapper.fromBytes(discoveryEvent.device().getLastDiscoveryTime().toDate(),
                    discoveryEvent.device().getManufacturerData());
        } catch (MapperException exception) {
            return;
        }

        SerialNumberHelper serialNumberHelper = new SerialNumberHelper(mCompatibleIds);
        String serialNumber = manufacturerData.getSerialNumber();

        if (!serialNumberHelper.serialNumberMatchesIdentifierArray(serialNumber)) {
            return;
        }

        Sensor sensor = mSensorCache.getExistingSensorFromSerialNumber(serialNumber);

        if (sensor == null) {
            discoveryEvent.device().setName(serialNumber);
            sensor = new SensorImpl(serialNumber, discoveryEvent.device());
            sensor.getVisibilityStatus().isBluestreamAdvertised();
            mSensorCache.addSensor(sensor);
        }

        Log.i(sensor.getSerialNumber() + " Advertised");
        Log.i(sensor.getSerialNumber() + " Results (Advertisement): " + manufacturerData.getHumidTempSample());

        if (mListener != null) {
            mListener.sensorDidAdvertise(sensor, manufacturerData, discoveryEvent.rssi());
        }
    }
}
