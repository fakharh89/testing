package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;

import io.blustream.sulley.routines.transactions.BootModeWriteFirmwareImageTransaction;
import io.blustream.sulley.sensor.Sensor;

public class WriteFirmwareImageRoutineImpl implements WriteFirmwareImageRoutine {
    private boolean simulateFailure;
    private OTAUBootModeBleDefinitions mDefinitions;
    private Listener mListener;
    private BootModeWriteFirmwareImageTransaction.Listener writeListener;
    private Sensor mSensor;
    private byte[] mImageToWrite;
    private int lastPercentComplete = -1;

    public WriteFirmwareImageRoutineImpl(@NonNull Sensor sensor, @NonNull byte[] imageToWrite, @NonNull Listener listener, @NonNull OTAUBootModeBleDefinitions definitions) {
        mListener = listener;
        mDefinitions = definitions;
        mSensor = sensor;
        mImageToWrite = new byte[imageToWrite.length];
        System.arraycopy(imageToWrite, 0, mImageToWrite, 0, imageToWrite.length);

        writeListener = new BootModeWriteFirmwareImageTransaction.Listener() {
            @Override
            public void onProgress(int percentComplete) {
                if (percentComplete != lastPercentComplete) {
                    mListener.writeProgress(percentComplete);
                    lastPercentComplete = percentComplete;
                }
            }

        };
    }

    @Override
    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    @Override
    public boolean start() {
        if (!isSensorCompatible(mSensor) ||
                mImageToWrite == null ||
                mImageToWrite.length == 0 ||
                mDefinitions == null) {
            return false;
        }

        if (mListener != null) {
            mListener.onWriteStarted();
        }
        return mSensor.getBleDevice().performTransaction(new BootModeWriteFirmwareImageTransaction(mImageToWrite, mDefinitions, writeListener, simulateFailure) {
            @Override
            protected void onEnd(BleDevice device, EndReason endReason) {
                super.onEnd(device, endReason);

                if (mListener == null) {
                    return;
                }

                if (endReason == EndReason.SUCCEEDED) {
                    mListener.onWriteComplete();
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
