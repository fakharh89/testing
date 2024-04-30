package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;
import com.idevicesinc.sweetblue.utils.Utils_Byte;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.BlinkMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.Status;
import io.blustream.sulley.routines.transactions.BufferDumpTransactionImpl;
import io.blustream.sulley.routines.transactions.CheckEditableSettingsTransaction;
import io.blustream.sulley.routines.transactions.SyncTimeTransaction;
import io.blustream.sulley.sensor.Sensor;

// TODO fix failure cases
// Slava suggests SingleThreadPoolExecutor as alternative to QueuedTransaction
// Oleksandr suggests using SweetBlue unit tests and mock objects
// Documentation for code before iOS library rewrite
// Documentation for classes and function
// Create functional data sheet for sensors?

// TODO Routines might want to include connections and disconnection events
// Move to command pattern with SensorManager
/*
SensorManager manager;
manager.startRoutine(new Routine(mSensor, listener));
 */

public class BlustreamRoutineImpl implements BlustreamRoutine<BlustreamRoutine.Listener,
        BlustreamRoutine.Settings> {
    protected final BleDefinitions mDefinitions;
    private final Options mOptions;
    private final BatterySubroutine mBatterySubroutine;
    private final StatusSubroutine mStatusSubroutine;
    Sensor mSensor;
    private Listener mListener;
    private Future<Settings> mFuture;

    BlustreamRoutineImpl(@NonNull Sensor sensor, @NonNull BleDefinitions definitions,
                         @NonNull Options options) {
        mDefinitions = definitions;
        mOptions = options;
        mSensor = sensor;
        mBatterySubroutine = new BatterySubroutineImpl(sensor, definitions);
        mStatusSubroutine = new StatusSubroutineImpl(sensor, definitions);
    }

    @Override
    public boolean isSensorCompatible(Sensor sensor) {
        return DefinitionCheckHelper.checkSensor(mDefinitions, sensor);
    }

    public boolean start() {
        if (!isSensorCompatible(mSensor)) {
            return false;
        }
        Log.d(getSensor().getSerialNumber() + " start. SyncTimeTransaction");

        boolean ranTransaction = getSensor().getBleDevice().performTransaction(new SyncTimeTransaction(mDefinitions) {
            @Override
            protected void onEnd(BleDevice device, EndReason endReason) {
                Log.d(getSensor().getSerialNumber() + " SyncTimeTransaction.onEnd");
                super.onEnd(device, endReason);
                if (endReason == EndReason.SUCCEEDED) {
                    onSyncTimeSuccess();
                } else {
                    // TODO write some actual retry code
                    Log.e(mSensor.getSerialNumber() + " Critical! Failed to sync time - routine has failed!");
                }
            }
        });

        if (ranTransaction) {
            Log.d(getSensor().getSerialNumber() + " syncTimeTransaction started");
        } else {
            Log.d(getSensor().getSerialNumber() + " syncTimeTransaction failed to start");
        }
        return ranTransaction;
    }

    protected void onSyncTimeSuccess() {
        Log.d(getSensor().getSerialNumber() + "onSyncTimeSuccess");
        if (mOptions.editSettingsBeforeGettingData()) {
            checkEditableSettings();
        }
        mStatusSubroutine.start();
        mBatterySubroutine.start();
    }

    @Override
    public boolean stop() {
        Log.d(getSensor().getSerialNumber() + " stop");
        mBatterySubroutine.stop();
        mStatusSubroutine.stop();
        return true;
    }

    @Override
    public Listener getListener() {
        Log.d(getSensor().getSerialNumber() + " getListener");
        return mListener;
    }

    @Override
    public void setListener(Listener listener) {
        Log.d(getSensor().getSerialNumber() + " setListener");
        mListener = listener;
        mBatterySubroutine.setListener(mListener);
        mStatusSubroutine.setListener(new StatusSubroutine.Listener() {
            // TODO This code won't run unless the user calls setListener!
            @Override
            public void didGetStatus(Status status) {
                Log.d(getSensor().getSerialNumber() + " didGetStatus");
                if (status.hasUnreadData()) {
                    Log.i(getSensor().getSerialNumber() + " Sensor has new data!");
                    startReadingBuffers();
                } else {
                    Log.i(getSensor().getSerialNumber() + " Sensor has no new data");
                }

                mListener.didGetStatus(status);
            }

            @Override
            public void didEncounterError() {
                Log.d(getSensor().getSerialNumber() + " didEncounterError");
                mListener.didEncounterError();
            }
        });
    }

    @Override
    public Sensor getSensor() {
        return mSensor;
    }

    synchronized private void startReadingBuffers() {
        Log.d(getSensor().getSerialNumber() + " startReadingBuffers");
        BufferDumpTransactionImpl transaction = new BufferDumpTransactionImpl(mDefinitions,
                mOptions.succeedOnDisconnectAfterDelete(), mListener) {
            @Override
            protected void onEnd(BleDevice device, EndReason endReason) {
                Log.d(getSensor().getSerialNumber() + " BufferDumpTransactionImpl.onEnd");
                super.onEnd(device, endReason);
                if (!mOptions.editSettingsBeforeGettingData()) {
                    checkEditableSettings();
                }
            }
        };

        boolean ranTransaction = getSensor().getBleDevice().performOta(transaction);
        if (ranTransaction) {
            Log.d(getSensor().getSerialNumber() + " Started BufferDumpTransaction");
        } else {
            Log.d(getSensor().getSerialNumber() + " Failed to start BufferDumpTransaction");
        }
    }

    @Override
    public boolean checkEditableSettings() {
        Log.d(getSensor().getSerialNumber() + " checkEditableSettings");
        CheckEditableSettingsTransaction checkEditableSettingsTransaction = createEditableSettingsTransaction();
        if (checkEditableSettingsTransaction != null) {
            boolean ranTransaction = getSensor().getBleDevice().performOta(checkEditableSettingsTransaction);
            if (ranTransaction) {
                Log.d(getSensor().getSerialNumber() + " Started checkEditableSettings");
            } else {
                Log.d(getSensor().getSerialNumber() + " Failed to start checkEditableSettings");
            }
            return ranTransaction;
        }
        Log.e(getSensor().getSerialNumber() + " Failed to start checkEditableSettings, CheckEditableSettingsTransaction was null!");
        return false;
    }

    private CheckEditableSettingsTransaction createEditableSettingsTransaction() {
        Log.d(getSensor().getSerialNumber() + "createEditableSettingsTransaction");
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

        return new CheckEditableSettingsTransaction(mDefinitions, editableSettings) {
            @Override
            public void onEnd(BleDevice device, BleTransaction.EndReason endReason) {
                Log.d(getSensor().getSerialNumber() + " CheckEditableSettingsTransaction.onEnd");
                super.onEnd(device, endReason);
                if (mListener == null) {
                    return;
                }

                if (endReason == EndReason.SUCCEEDED) {
                    mListener.didConfirmEditableSettings(mSensor);
                } else {
                    mListener.didFailConfirmEditableSettings(mSensor);
                }

                // TODO I think this workflow is messed up.  Let's rethink it
                // We need some way to queue up transactions
                if (mOptions.editSettingsBeforeGettingData()) {
                    startReadingBuffers();
                }
            }
        };
    }

    @Override
    public Listener getEditableListener() {
        Log.d(getSensor().getSerialNumber() + " getEditableListener");
        return mListener;
    }

    @Override
    public void setEditableListener(Listener listener) {
        Log.w(getSensor().getSerialNumber() + "setEditableListener. Warning: You likely meant to call the setListener.  This has the same functionality");
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
    public boolean blink(int count) {
        Log.d(getSensor().getSerialNumber() + " blink");
        BlinkMapper mapper = new BlinkMapper();
        byte[] bytes;
        try {
            bytes = mapper.toBytes(count);
            Utils_Byte.reverseBytes(bytes);
        } catch (MapperException e) {
            Log.w(getSensor().getSerialNumber() + " Failed to create bytes for blink count!");
            return false;
        }

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(mSensor.getBleDevice().getName_override() + " Wrote blink command");
            } else {
                Log.e(mSensor.getBleDevice().getName_override() + " Failed to write blink command");
            }
        };

        BleDevice.ReadWriteListener.ReadWriteEvent event = getSensor().getBleDevice().write(mDefinitions.getBlinkService(), mDefinitions.getBlinkCharacteristic(), bytes, listener);
        if (event.status() == BleDevice.ReadWriteListener.Status.SUCCESS) {
            Log.i(getSensor().getSerialNumber() + " Wrote blink command!");
            return true;
        } else if (event.isNull()) {
            Log.i(getSensor().getSerialNumber() + " Blink command was added to SB Queue!");
            return true;
        } else {
            Log.w(getSensor().getSerialNumber() + " Failed to write blink command! " + event.status().name());
            return false;
        }
    }

    @Override
    public boolean readHumidTemp() {
        Log.d(getSensor().getSerialNumber() + " readHumidTemp.RealtimeTransaction");
        byte[] data = new byte[1];
        data[0] = 0;

        Log.i(mSensor.getBleDevice().getName_override() + " Writing realtime data command");

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(mSensor.getBleDevice().getName_override() + " Wrote realtime data command");
            } else {
                Log.e(mSensor.getBleDevice().getName_override() + " Failed to write realtime data command!");
            }
        };

        BleDevice.ReadWriteListener.ReadWriteEvent event = mSensor.getBleDevice().write(mDefinitions.getRealtimeService(),
                mDefinitions.getRealtimeHumidTempCharacteristic(), data, listener);

        if (event.status() == BleDevice.ReadWriteListener.Status.SUCCESS) {
            Log.i(getSensor().getSerialNumber() + " Wrote realtime command!");
            return true;
        } else if (event.isNull()) {
            Log.i(getSensor().getSerialNumber() + " Realtime command was added to SB Queue!");
            return true;
        } else {
            Log.w(getSensor().getSerialNumber() + " Failed to write realtime command!");
            return false;
        }
    }

    @Override
    public boolean readBattery() {
        Log.d(getSensor().getSerialNumber() + " readBattery");
        return mBatterySubroutine.readBattery();
    }
}
