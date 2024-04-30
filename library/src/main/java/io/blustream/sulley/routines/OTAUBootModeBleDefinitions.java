package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.blustream.sulley.utilities.Uuids;

public class OTAUBootModeBleDefinitions {

    private UUID getBootModeService() {
        return Uuids.Otau.Boot.BOOT_OTAU_SERVICE;
    }

    private UUID getBootModeKeyBlockCharacteristic() {
        return Uuids.Otau.Boot.KEY_BLOCK_CHARACTERISTIC;
    }

    private UUID getBootModeDataTransferCharacteristic() {
        return Uuids.Otau.Boot.DATA_TRANSFER_CHARACTERISTIC;
    }

    private UUID getBootModeControlTransferCharacteristic() {
        return Uuids.Otau.Boot.CONTROL_TRANSFER_CHARACTERISTIC;
    }

    @NonNull
    @BleCharacteristic
    public UUID getBootModeOTAUVersionCharacteristic() {
        return Uuids.Otau.Boot.OTAU_VERSION_CHARACTERISTIC;
    }

    @NonNull
    @BleService
    public UUID getBootModeOTAUVersionService() {
        return getBootModeService();
    }

    @NonNull
    @BleService
    public UUID getSetAppModeService() {
        return getBootModeService();
    }

    @NonNull
    @BleService
    public UUID getBootModeOTAUPropertiesService() {
        return getBootModeService();
    }

    @NonNull
    @BleCharacteristic
    public UUID getBootModeOTAUPropertiesKeyBlockCharacteristic() {
        return getBootModeKeyBlockCharacteristic();
    }

    @NonNull
    @BleCharacteristic
    public UUID getBootModeOTAUPropertiesDataTransferCharacteristic() {
        return getBootModeDataTransferCharacteristic();
    }

    @NonNull
    @BleService
    public UUID getBootModeCrystalTrimService() {
        return getBootModeService();
    }

    @NonNull
    @BleCharacteristic
    public UUID getBootModeCrystalTrimKeyBlockCharacteristic() {
        return getBootModeKeyBlockCharacteristic();
    }

    @NonNull
    @BleCharacteristic
    public UUID getBootModeCrystalTrimDataTransferCharacteristic() {
        return getBootModeDataTransferCharacteristic();
    }

    @NonNull
    @BleService
    public UUID getBootModeWriteFirmwareService() {
        return getBootModeService();
    }

    @NonNull
    @BleCharacteristic
    public UUID getBootModeWriteFirmwareControlTransferCharacteristic() {
        return getBootModeControlTransferCharacteristic();
    }

    @NonNull
    @BleCharacteristic
    public UUID getBootModeWriteFirmwareDataTransferCharacteristic() {
        return getBootModeDataTransferCharacteristic();
    }

    @NonNull
    @BleCharacteristic
    public UUID getSetAppModeControlTransferCharacteristic() {
        return getBootModeControlTransferCharacteristic();
    }

}
