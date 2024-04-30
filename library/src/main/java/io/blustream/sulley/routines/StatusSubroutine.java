package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.blustream.sulley.models.Status;

public interface StatusSubroutine extends Routine<StatusSubroutine.Listener> {

    interface BleDefinitions {
        @NonNull
        @BleService
        UUID getStatusService();

        @NonNull
        @BleCharacteristic
        UUID getStatusCharacteristic();

    }

    interface Listener extends Routine.Listener {
        void didGetStatus(Status status);
    }
}
