package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import io.blustream.sulley.sensor.Sensor;

public class V3RoutineImpl extends BlustreamRoutineImpl {
    public V3RoutineImpl(@NonNull Sensor sensor) {
        super(sensor, new V3BleDefinitions(), getOptions());
    }

    private static Options getOptions() {
        return new Options() {
            @Override
            public boolean succeedOnDisconnectAfterDelete() {
                return false;
            }

            @Override
            public boolean editSettingsBeforeGettingData() {
                return false;
            }
        };
    }

    @Override
    public boolean isSensorCompatible(Sensor sensor) {
        boolean areCharsComp = DefinitionCheckHelper.checkSensor(mDefinitions, sensor);
        boolean isV3 = sensor.getSoftwareVersion().startsWith("3");
        return areCharsComp && isV3;
    }
}
