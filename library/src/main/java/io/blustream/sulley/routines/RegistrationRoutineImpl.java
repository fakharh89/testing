package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;

import io.blustream.sulley.routines.transactions.RegistrationTransaction;
import io.blustream.sulley.sensor.Sensor;

public class RegistrationRoutineImpl implements RegistrationRoutine {

    @NonNull
    private final BleDefinitions mDefinitions;
    private RegistrationRoutine.Listener mListener;
    private Sensor mSensor;

    public RegistrationRoutineImpl(@NonNull Sensor sensor, @NonNull BleDefinitions definitions) {
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

        return mSensor.getBleDevice().performTransaction(new RegistrationTransaction(mDefinitions) {
            @Override
            protected void onEnd(BleDevice device, EndReason endReason) {
                super.onEnd(device, endReason);

                if (mListener == null) {
                    return;
                }

                if (endReason == EndReason.SUCCEEDED) {
                    mListener.onRegistrationSuccess();
                } else {
                    mListener.onRegistrationFailure();
                }
            }
        });
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public Listener getListener() {
        return mListener;
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public Sensor getSensor() {
        return mSensor;
    }
}
