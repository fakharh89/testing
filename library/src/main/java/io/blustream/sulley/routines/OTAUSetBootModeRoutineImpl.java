package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;

import io.blustream.logger.Log;
import io.blustream.sulley.sensor.Sensor;

public class OTAUSetBootModeRoutineImpl implements OTAUSetBootModeRoutine {
    private OTAUBleDefinitions definitions;
    private Sensor sensor;
    private Listener listener;

    public OTAUSetBootModeRoutineImpl(@NonNull Sensor sensor, @NonNull OTAUBleDefinitions definitions) {
        this.definitions = definitions;
        this.sensor = sensor;
    }

    @Override
    public boolean start() {
        if (!isSensorCompatible(sensor)) {
            return false;
        }

        return sensor.getBleDevice().performTransaction(new BleTransaction() {
            @Override
            protected void start(BleDevice device) {
                BleDevice.ReadWriteListener listener = readWriteEvent -> {
                    if (readWriteEvent.wasSuccess()) {
                        Log.i(getDevice().getName_override() + " Wrote boot mode command");
                        sensor.disconnect();
                        if (getListener() != null) {
                            getListener().onSuccess();
                        }
                        succeed();
                    } else {
                        Log.e(getDevice().getName_override() + " Failed to write boot mode command");
                        if (getListener() != null) {
                            getListener().didEncounterError();
                        }
                        fail();
                    }


                };

                getDevice().write(definitions.getApplicationService(), definitions.getCurrentApplicationCharacteristic(), new byte[] {(byte)1}, listener);
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
