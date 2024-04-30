package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.blustream.sulley.utilities.Uuids;

public class V3BleDefinitions implements BlustreamRoutine.BleDefinitions, RegistrationRoutine.BleDefinitions {

    @NonNull
    @BleService
    private UUID getBlustreamService() {
        return Uuids.v3.BLUSTREAM_SERVICE;
    }

    @NonNull
    @Override
    @BleService
    public UUID getTimeSyncService() {
        return getBlustreamService();
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getTimeSyncCharacteristic() {
        return Uuids.v3.TIME_SYNC_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getRegistrationService() {
        return getBlustreamService();
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getRegistrationCharacteristic() {
        return Uuids.v3.REGISTRATION_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getBlinkService() {
        return getBlustreamService();
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getBlinkCharacteristic() {
        return Uuids.v3.BLINK_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getHumidTempSamplingIntervalService() {
        return getBlustreamService();
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getHumidTempSamplingIntervalCharacteristic() {
        return Uuids.v3.HUMID_TEMP_SAMPLING_INTERVAL_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getAccelerometerModeService() {
        return getBlustreamService();
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getAccelerometerModeCharacteristic() {
        return Uuids.v3.ACCELEROMETER_MODE_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getImpactThresholdService() {
        return getBlustreamService();
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getImpactThresholdCharacteristic() {
        return Uuids.v3.IMPACT_THRESHOLD_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getStatusCharacteristic() {
        return Uuids.v3.STATUS_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getStatusService() {
        return getBlustreamService();
    }

    @Override
    @NonNull
    @BleService
    public UUID getBatteryService() {
        return com.idevicesinc.sweetblue.utils.Uuids.BATTERY_SERVICE_UUID;
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getBatteryCharacteristic() {
        return com.idevicesinc.sweetblue.utils.Uuids.BATTERY_LEVEL;
    }

    @NonNull
    @Override
    @BleService
    public UUID getBufferService() {
        return getBlustreamService();
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getHumidTempBufferCharacteristic() {
        return Uuids.v3.HUMID_TEMP_BUFFER_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getHumidTempBufferSizeCharacteristic() {
        return Uuids.v3.HUMID_TEMP_BUFFER_SIZE_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getImpactBufferCharacteristic() {
        return Uuids.v3.IMPACT_BUFFER_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getImpactBufferSizeCharacteristic() {
        return Uuids.v3.IMPACT_BUFFER_SIZE_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getMotionBufferCharacteristic() {
        return Uuids.v3.MOTION_BUFFER_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getMotionBufferSizeCharacteristic() {
        return Uuids.v3.MOTION_BUFFER_SIZE_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getRealtimeService() {
        return getBlustreamService();
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getRealtimeHumidTempCharacteristic() {
        return getHumidTempBufferCharacteristic();
    }

}
