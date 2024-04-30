package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.utils.Interval;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.StatusMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.Status;
import io.blustream.sulley.sensor.Sensor;

public final class StatusSubroutineImpl implements StatusSubroutine {
    @NonNull
    private final Sensor mSensor;

    @NonNull
    private final StatusSubroutine.BleDefinitions mDefinitions;
    private final Interval mStatusInterval = Interval.secs(20);
    private StatusSubroutine.Listener mListener;
    private BleDevice.ReadWriteListener mReadWriteListener;

    public StatusSubroutineImpl(@NonNull Sensor sensor, @NonNull StatusSubroutine.BleDefinitions definitions) {
        mSensor = sensor;
        mDefinitions = definitions;
    }

    @Override
    public boolean isSensorCompatible(Sensor sensor) {
        return DefinitionCheckHelper.checkSensor(mDefinitions, sensor);
    }

    @Override
    public boolean start() {
        if (!isSensorCompatible(mSensor)) {
            return false;
        }
        startStatusChangeTracking();
        return true;
    }

    @Override
    public boolean stop() {
        mSensor.getBleDevice().stopPoll(mDefinitions.getStatusService(),
                mDefinitions.getStatusCharacteristic(), mStatusInterval, mReadWriteListener);
        return true;
    }

    @Override
    public StatusSubroutine.Listener getListener() {
        return mListener;
    }

    @Override
    public void setListener(StatusSubroutine.Listener listener) {
        mListener = listener;
    }

    @Override
    public Sensor getSensor() {
        return mSensor;
    }

    private void startStatusChangeTracking() {
        mReadWriteListener = readWriteEvent -> {
            if (!readWriteEvent.wasSuccess()) {
                Log.e(getSensor().getSerialNumber() + " Status notify failed! " + readWriteEvent.status());
                return;
            }

            Log.i(getSensor().getSerialNumber() + " Got status notify!");
            try {
                StatusMapper mapper = new StatusMapper();
                Status status = mapper.fromBytes(readWriteEvent.data());

                if (mListener != null) {
                    mListener.didGetStatus(status);
                }
            } catch (MapperException e) {
                // TODO Handle this error
                Log.e(getSensor().getSerialNumber() + " Failed to map status! " + e);
            }
        };

        mSensor.getBleDevice().startPoll(mDefinitions.getStatusService(),
                mDefinitions.getStatusCharacteristic(), mStatusInterval, mReadWriteListener);
    }
}
