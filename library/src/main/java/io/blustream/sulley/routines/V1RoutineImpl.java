package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;

import io.blustream.logger.Log;
import io.blustream.sulley.routines.transactions.BlinkTransaction;
import io.blustream.sulley.routines.transactions.CheckEditableSettingsTransaction;
import io.blustream.sulley.routines.transactions.RealtimeTransaction;
import io.blustream.sulley.sensor.Sensor;

public class V1RoutineImpl implements V1Routine {

    private final BleDefinitions mDefinitions = new V1BleDefinitions();
    private final BatterySubroutine mBatterySubroutine;
    private final StatusSubroutine mStatusSubroutine;
    private final V1DataSubroutine mV1DataSubroutine;

    private Listener mListener;
    private Future<Settings> mFuture;
    private Sensor mSensor;

    public V1RoutineImpl(@NonNull Sensor sensor) {
        mSensor = sensor;
        mBatterySubroutine = new BatterySubroutineImpl(sensor, mDefinitions);
        mStatusSubroutine = new StatusSubroutineImpl(sensor, mDefinitions);
        mV1DataSubroutine = new V1DataSubroutineImpl(sensor, mDefinitions);
    }

    @Override
    public boolean blink(int count) {
        boolean startedTransaction = runTransaction(new BlinkTransaction(mDefinitions, count));
        if (startedTransaction) {
            Log.i(getSensor().getSerialNumber() + " Started blink transaction!");
        } else {
            Log.w(getSensor().getSerialNumber() + " Failed to start blink transaction!");
        }
        return startedTransaction;
    }

    @Override
    public boolean checkEditableSettings() {
        if (!runTransaction(prepareEditableSettingsTransaction())) {
            Log.i(getSensor().getSerialNumber() + " Couldn't start transaction to check settings! Queueing for later");
        }

        return true;
    }

    private CheckEditableSettingsTransaction prepareEditableSettingsTransaction() {
        if (mFuture == null) {
            Log.w(getSensor().getSerialNumber() + " Fail - Settings future is null!");
            return null;
        }

        Settings editableSettings = mFuture.getEditableSettings(mSensor);

        if (editableSettings == null) {
            Log.w(getSensor().getSerialNumber() + " Fail - Settings are null!");
            return null;
        }

        try {
            editableSettings.validate();
        } catch (Editable.Settings.InvalidSettingsException e) {
            Log.e(getSensor().getSerialNumber() + " Fail - Settings are invalid!");
            return null;
        }

        CheckEditableSettingsTransaction transaction = new CheckEditableSettingsTransaction(mDefinitions, editableSettings) {
            @Override
            public void onEnd(BleDevice device, BleTransaction.EndReason endReason) {
                super.onEnd(device, endReason);
                if (mListener == null) {
                    return;
                }

                if (endReason == EndReason.SUCCEEDED) {
                    mListener.didConfirmEditableSettings(mSensor);
                } else {
                    mListener.didFailConfirmEditableSettings(mSensor);
                }
            }
        };

        return transaction;
    }

    @Override
    public Listener getEditableListener() {
        return mListener;
    }

    @Override
    public void setEditableListener(Listener listener) {
        Log.w(getSensor().getSerialNumber() + " Warning: You likely meant to call the setListener.  This has the same functionality");
        this.setListener(listener);
    }

    @Override
    public Future<Settings> getEditableSettingsFuture() {
        return mFuture;
    }

    @Override
    public void setEditableSettingsFuture(Future<Settings> future) {
        mFuture = future;
    }

    @Override
    public boolean readHumidTemp() {
        boolean startedTransaction = runTransaction(new RealtimeTransaction(mDefinitions));

        if (startedTransaction) {
            Log.i(getSensor().getSerialNumber() + " Started realtime transaction!");
        } else {
            Log.w(getSensor().getSerialNumber() + " Failed to start realtime transaction!");
        }

        return startedTransaction;
    }

    @Override
    public boolean readBattery() {
        return mBatterySubroutine.readBattery();
    }

    @Override
    public boolean start() {
        if (!isSensorCompatible(mSensor)) {
            return false;
        }
        checkEditableSettings();

        mV1DataSubroutine.start();
        mStatusSubroutine.start();
        mBatterySubroutine.start();

        return true;
    }

    @Override
    public boolean stop() {
        mV1DataSubroutine.stop();
        mBatterySubroutine.stop();
        mStatusSubroutine.stop();
        return true;
    }

    @Override
    public Listener getListener() {
        return mListener;
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
        mV1DataSubroutine.setListener(mListener);
        mBatterySubroutine.setListener(mListener);
        mStatusSubroutine.setListener(mListener);
    }

    @Override
    public Sensor getSensor() {
        return mSensor;
    }

    protected boolean runTransaction(BleTransaction transaction) {
        return mSensor.getBleDevice().performTransaction(transaction);
    }

    @Override
    public boolean isSensorCompatible(Sensor sensor) {
        boolean areCharsComp = DefinitionCheckHelper.checkSensor(mDefinitions, sensor);
        boolean isV1 = sensor.getSoftwareVersion().startsWith("1");
        return areCharsComp && isV1;
    }
}
