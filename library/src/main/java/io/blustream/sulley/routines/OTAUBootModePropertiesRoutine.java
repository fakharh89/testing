package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.blustream.sulley.otau.model.FirmwareProperties;

public interface OTAUBootModePropertiesRoutine extends Routine<OTAUBootModePropertiesRoutine.Listener> {

    interface Listener extends Routine.Listener {
        void onSuccess(FirmwareProperties firmwareProperties);
    }
}
