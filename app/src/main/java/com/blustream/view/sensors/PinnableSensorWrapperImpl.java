package com.blustream.view.sensors;

import io.blustream.sulley.sensor.Sensor;

/**
 * Created by Ruzhitskii Sviatoslav on 10/30/19.
 */
public class PinnableSensorWrapperImpl implements PinnableSensorWrapper {

    private Sensor sensor;

    private boolean isPinned;

    public PinnableSensorWrapperImpl(Sensor other) {
        this.sensor = other;
    }

    @Override
    public Sensor getSensor() {
        return sensor;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }
}
