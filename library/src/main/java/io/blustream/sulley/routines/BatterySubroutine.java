package io.blustream.sulley.routines;

import java.util.UUID;

import androidx.annotation.NonNull;
import io.blustream.sulley.models.BatterySample;

public interface BatterySubroutine extends Routine<BatterySubroutine.Listener>, Realtime.Battery {

    interface BleDefinitions {
        @NonNull
        @BleService
        UUID getBatteryService();

        @NonNull
        @BleCharacteristic
        UUID getBatteryCharacteristic();
    }

    interface Listener extends Routine.Listener {
        void didGetBatterySample(BatterySample batterySample);
    }
}
