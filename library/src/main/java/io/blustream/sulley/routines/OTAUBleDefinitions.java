package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.blustream.sulley.utilities.Uuids;

public class OTAUBleDefinitions implements OTAUPropertiesRoutine.BleDefinitions {
    @NonNull
    private UUID getAppService() {
        return Uuids.Otau.Application.APPLICATION_OTAU_SERVICE;
    }

    @NonNull
    private UUID getAppVersionCharacteristic() {
        return Uuids.Otau.Application.OTAU_VERSION_CHARACTERISTIC;
    }

    @NonNull
    private UUID getAppCurrentCharacteristic() {
        return Uuids.Otau.Application.CURRENT_APPLICATION_CHARACTERISTIC;
    }

    @NonNull
    private UUID getAppDataTransferCharacteristic() {
        return Uuids.Otau.Application.DATA_TRANSFER_CHARACTERISTIC;
    }

    @NonNull
    private UUID getAppKeyBlockCharacteristic() {
        return Uuids.Otau.Application.KEY_BLOCK_CHARACTERISTIC;
    }

    @NonNull
    @Override
    public UUID getApplicationService() {
        return getAppService();
    }

    @NonNull
    @Override
    public UUID getKeyBlockCharacteristic() {
        return getAppKeyBlockCharacteristic();
    }

    @NonNull
    @Override
    public UUID getDataTransferCharacteristic() {
        return getAppDataTransferCharacteristic();
    }

    @NonNull
    @Override
    public UUID getOTAUVersionCharacteristic() {
        return getAppVersionCharacteristic();
    }

    @NonNull
    @Override
    public UUID getCurrentApplicationCharacteristic() {
        return getAppCurrentCharacteristic();
    }
}
