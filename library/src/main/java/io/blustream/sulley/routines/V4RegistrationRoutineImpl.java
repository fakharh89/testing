package io.blustream.sulley.routines;

import androidx.annotation.NonNull;
import io.blustream.sulley.sensor.Sensor;

public class V4RegistrationRoutineImpl extends RegistrationRoutineImpl {
    public V4RegistrationRoutineImpl(@NonNull Sensor sensor) {
        super(sensor, new V4BleDefinitions());
    }
}
