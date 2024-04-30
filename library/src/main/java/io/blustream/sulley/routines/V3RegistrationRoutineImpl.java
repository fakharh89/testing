package io.blustream.sulley.routines;

import androidx.annotation.NonNull;
import io.blustream.sulley.sensor.Sensor;

public class V3RegistrationRoutineImpl extends RegistrationRoutineImpl {
    public V3RegistrationRoutineImpl(@NonNull Sensor sensor) {
        super(sensor, new V3BleDefinitions());
    }
}
