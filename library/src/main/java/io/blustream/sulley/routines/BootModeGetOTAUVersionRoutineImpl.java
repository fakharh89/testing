package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;

import io.blustream.sulley.routines.transactions.subtransactions.BootModeOTAUVersionSubTransaction;
import io.blustream.sulley.sensor.Sensor;

public class BootModeGetOTAUVersionRoutineImpl implements BootModeGetOTAUVersionRoutine {
    @NonNull
    private final OTAUBootModeBleDefinitions mDefinitions;
    private BootModeGetOTAUVersionRoutine.Listener mListener;
    private Sensor mSensor;

    public BootModeGetOTAUVersionRoutineImpl(@NonNull Sensor sensor, @NonNull OTAUBootModeBleDefinitions definitions) {
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

        return mSensor.getBleDevice().performTransaction(new BootModeOTAUVersionSubTransaction(mDefinitions) {
            @Override
            protected void onEnd(BleDevice device, EndReason endReason) {
                super.onEnd(device, endReason);

                if (mListener == null) {
                    return;
                }

                if (endReason == EndReason.SUCCEEDED) {
                    mListener.onOTAUVersionSuccess(getOtauVersion());
                } else {
                    mListener.didEncounterError();
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
