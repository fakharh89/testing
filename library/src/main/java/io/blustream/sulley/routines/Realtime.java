package io.blustream.sulley.routines;

public interface Realtime {
    interface HumidTemp {
        boolean readHumidTemp();
    }

    interface Battery {
        boolean readBattery();
    }
}
