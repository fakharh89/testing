package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.blustream.sulley.otau.model.FirmwareProperties;

public interface OTAUPropertiesRoutine extends Routine<OTAUPropertiesRoutine.Listener> {

    interface BleDefinitions {
        @NonNull
        @BleService
        UUID getApplicationService();

        @NonNull
        @BleCharacteristic
        UUID getKeyBlockCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getDataTransferCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getOTAUVersionCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getCurrentApplicationCharacteristic();
    }

    interface Listener extends Routine.Listener {
        void onSuccess(FirmwareProperties properties);
    }
}
