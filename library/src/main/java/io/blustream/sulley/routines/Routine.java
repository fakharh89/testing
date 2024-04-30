package io.blustream.sulley.routines;

import io.blustream.sulley.sensor.Sensor;

public interface Routine<T extends Routine.Listener> {
    boolean start();

    boolean stop();

    T getListener();

    void setListener(T listener);

    Sensor getSensor();

    boolean isSensorCompatible(Sensor sensor);

    interface Listener {
        void didEncounterError(); // TODO figure out how to expose errors
    }
}
