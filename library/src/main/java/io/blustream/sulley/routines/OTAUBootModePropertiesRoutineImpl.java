package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;

import java.util.UUID;

import io.blustream.logger.Log;
import io.blustream.sulley.otau.model.FirmwareProperties;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.routines.transactions.OTAUBootModePropertiesTransaction;
import io.blustream.sulley.routines.transactions.subtransactions.BootModeCrystalTrimSubTransaction;
import io.blustream.sulley.sensor.Sensor;

public class OTAUBootModePropertiesRoutineImpl implements OTAUBootModePropertiesRoutine {
    @NonNull
    private final Sensor mSensor;
    @NonNull
    private final OTAUBootModeBleDefinitions mDefinitions;
    @NonNull
    private final PsKeyDatabase mPsKeyDatabase;
    private Listener mListener;
    private FirmwareProperties mFirmwareProperties;
    private int mCrystalTrim;

    public OTAUBootModePropertiesRoutineImpl(@NonNull Sensor sensor, @NonNull OTAUBootModeBleDefinitions definitions,
                                             @NonNull PsKeyDatabase psKeyDatabase) {
        mSensor = sensor;
        mDefinitions = definitions;
        mPsKeyDatabase = psKeyDatabase;
    }

    @Override
    public boolean start() {
        if (!isSensorCompatible(mSensor)) {
            return false;
        }
        return runOTAUBootModeCrystalTrimTransaction();
    }

    private boolean runOTAUBootModeCrystalTrimTransaction() {
        OTAUBootModeBleDefinitions definitions = new OTAUBootModeBleDefinitions();

        BootModeCrystalTrimSubTransaction transaction = new BootModeCrystalTrimSubTransaction(definitions) {
            @Override
            protected void onEnd(BleDevice device, EndReason endReason) {
                super.onEnd(device, endReason);

                if (mListener == null) {
                    return;
                }

                if (endReason == EndReason.SUCCEEDED) {
                    mCrystalTrim = getCrystalTrim();
                    runOTAUBootModePropertiesTransaction();
                } else {
                    mListener.didEncounterError();
                }
            }

        };

        mSensor.getBleDevice().performTransaction(transaction);
        return true;
    }

    private boolean runOTAUBootModePropertiesTransaction() {

        OTAUBootModePropertiesTransaction transaction = new OTAUBootModePropertiesTransaction(mDefinitions, mPsKeyDatabase) {
            @Override
            protected void onEnd(BleDevice device, EndReason endReason) {
                super.onEnd(device, endReason);
                Log.i("asdf.onEnd");
                mFirmwareProperties = this.getFirmwareProperties();
                mFirmwareProperties.setCrystalTrim(mCrystalTrim);
                if (mListener == null) {
                    return;
                }

                if (endReason == EndReason.SUCCEEDED) {
                    mListener.onSuccess(mFirmwareProperties);
                } else {
                    mListener.didEncounterError();
                }
//                byte[] bytes = ByteBuffer.allocate(2).putShort((short) 1).array();
//
//                mSensor.getBleDevice().write(mDefinitions.getApplicationService(),
//                        mDefinitions.getCurrentApplicationCharacteristic(), bytes, e -> {
//                            Log.d("OnEvent"+ e);
//
//                        });
            }
        };

        mSensor.getBleDevice().performTransaction(transaction);
        return true;
    }


    @Override
    public boolean stop() {
        return false;
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
