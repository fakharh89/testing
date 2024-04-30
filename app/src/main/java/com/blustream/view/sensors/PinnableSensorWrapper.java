package com.blustream.view.sensors;

import io.blustream.sulley.sensor.Sensor;

/**
 * Created by Ruzhitskii Sviatoslav on 10/30/19.
 */
public interface PinnableSensorWrapper {

    boolean isPinned();

    void setPinned(boolean pinned);

    Sensor getSensor();
}
