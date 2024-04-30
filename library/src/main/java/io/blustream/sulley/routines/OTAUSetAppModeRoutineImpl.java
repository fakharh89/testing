package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;

import io.blustream.logger.Log;
import io.blustream.sulley.sensor.Sensor;

public class OTAUSetAppModeRoutineImpl implements OTAUSetAppModeRoutine {
    private OTAUBootModeBleDefinitions definitions;
    private Sensor sensor;
    private Listener listener;

    public OTAUSetAppModeRoutineImpl(@NonNull Sensor sensor, @NonNull OTAUBootModeBleDefinitions definitions) {
        this.definitions = definitions;
        this.sensor = sensor;
    }

    @Override
    public boolean start() {
        if (!isSensorCompatible(sensor)) {
            return false;
        }

        return sensor.getBleDevice().performOta(new BleTransaction.Ota() {
            @Override
            protected void start(BleDevice device) {
                BleDevice.ReadWriteListener listener = readWriteEvent -> {
                    if (readWriteEvent.wasSuccess()) {
                        Log.i(getDevice().getName_override() + " Wrote app mode command");
                        sensor.getBleDevice().unbond();
                        sensor.disconnect();
                        if (getListener() != null) {
                            getListener().onSuccess();
                        }
                        succeed();
                    } else {
                        Log.e(getDevice().getName_override() + " Failed to write app mode command");
                        if (getListener() != null) {
                            getListener().didEncounterError();
                        }
                        fail();
                    }


                };

                getDevice().write(definitions.getSetAppModeService(), definitions.getSetAppModeControlTransferCharacteristic(), new byte[] {(byte)4}, listener);
            }
        });
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Sensor getSensor() {
        return sensor;
    }

    @Override
    public boolean isSensorCompatible(Sensor sensor) {
        return DefinitionCheckHelper.checkSensor(definitions, sensor);
    }
}