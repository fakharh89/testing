package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import io.blustream.logger.Log;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.routines.transactions.OTAUPropertiesTransaction;
import io.blustream.sulley.sensor.Sensor;

public final class OTAUPropertiesRoutineImpl implements OTAUPropertiesRoutine {
    @NonNull
    private final Sensor mSensor;
    @NonNull
    private final OTAUBleDefinitions mDefinitions;
    @NonNull
    private final PsKeyDatabase mPsKeyDatabase;
    private Listener mListener;


    public OTAUPropertiesRoutineImpl(@NonNull Sensor sensor, @NonNull OTAUBleDefinitions definitions,
                               @NonNull PsKeyDatabase psKeyDatabase) {
        mSensor = sensor;
        mDefinitions = definitions;
        mPsKeyDatabase = psKeyDatabase;
    }

    @Override
    public boolean start() {

        OTAUPropertiesTransaction transaction = new OTAUPropertiesTransaction(mDefinitions, mPsKeyDatabase) {
            @Override
            protected void onEnd(BleDevice device, EndReason endReason) {
                super.onEnd(device, endReason);
                Log.i("asdf.onEnd");
                if (mListener != null) {
                    mListener.onSuccess(getFirmwareProperties());
                }
            }
        };

        return mSensor.getBleDevice().performTransaction(transaction);
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

    @Override
    public boolean isSensorCompatible(Sensor sensor) {
        return DefinitionCheckHelper.checkSensor(mDefinitions, sensor);
    }
}
