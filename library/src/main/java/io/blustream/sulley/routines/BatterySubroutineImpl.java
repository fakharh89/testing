package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.utils.Interval;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.BatteryMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.BatterySample;
import io.blustream.sulley.sensor.Sensor;

public final class BatterySubroutineImpl implements BatterySubroutine {
    @NonNull
    private final Sensor mSensor;

    @NonNull
    private final BatterySubroutine.BleDefinitions mDefinitions;
    private final Interval mBatteryInterval = Interval.mins(30);
    private BatterySubroutine.Listener mListener;
    private BleDevice.ReadWriteListener mReadWriteListener;

    public BatterySubroutineImpl(@NonNull Sensor sensor, @NonNull BatterySubroutine.BleDefinitions definitions) {
        mSensor = sensor;
        mDefinitions = definitions;
    }

    @Override
    public boolean start() {
        if (!isSensorCompatible(mSensor)) {
            return false;
        }
        startBatteryChangeTracking();
        return true;
    }

    @Override
    public boolean stop() {
        mSensor.getBleDevice().stopPoll(mDefinitions.getBatteryService(),
                mDefinitions.getBatteryCharacteristic(), mBatteryInterval, mReadWriteListener);
        return true;
    }

    // Not using transaction executor
    @Override
    public boolean readBattery() {
        return mSensor.getBleDevice().read(mDefinitions.getBatteryService(),
                mDefinitions.getBatteryCharacteristic(), mReadWriteListener).isNull();
    }

    @Override
    public BatterySubroutine.Listener getListener() {
        return mListener;
    }

    @Override
    public void setListener(BatterySubroutine.Listener listener) {
        mListener = listener;
    }

    @Override
    public Sensor getSensor() {
        return mSensor;
    }

    @Override
    public boolean isSensorCompatible(Sensor sensor) {
        return DefinitionCheckHelper.checkSensor(mDefinitions, sensor);
    }

    private void startBatteryChangeTracking() {
        mReadWriteListener = readWriteEvent -> {
            if (!readWriteEvent.wasSuccess()) {
                Log.e(getSensor().getSerialNumber() + " Battery notify failed! " + readWriteEvent.status());
                return;
            }

            Log.i(getSensor().getSerialNumber() + " Got battery notify!");

            try {
                BatteryMapper mapper = new BatteryMapper();
                BatterySample batterySample = mapper.fromBytes(readWriteEvent.data());

                Log.i(getSensor().getSerialNumber() + " Battery: " + batterySample);

                if (mListener != null) {
                    mListener.didGetBatterySample(batterySample);
                }
            } catch (MapperException e) {
                // TODO Handle this error
                Log.e(getSensor().getSerialNumber() + " Failed to map battery! " + e);
            }
        };

        mSensor.getBleDevice().startPoll(mDefinitions.getBatteryService(),
                mDefinitions.getBatteryCharacteristic(), mBatteryInterval, mReadWriteListener);
    }
}
