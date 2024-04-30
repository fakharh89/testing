package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.blustream.sulley.routines.transactions.subtransactions.BeaconModeTransaction;
import io.blustream.sulley.utilities.Uuids;

public class V4BleDefinitions implements BlustreamRoutine.BleDefinitions, RegistrationRoutine.BleDefinitions,
        BeaconModeTransaction.BleDefinitions  {

    @NonNull
    private UUID getBlustreamService() {
        return Uuids.v4.BLUSTREAM_SERVICE;
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
        return Uuids.v4.TIME_SYNC_CHARACTERISTIC;
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
        return Uuids.v4.REGISTRATION_CHARACTERISTIC;
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
        return Uuids.v4.BLINK_CHARACTERISTIC;
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
        return Uuids.v4.HUMID_TEMP_SAMPLING_INTERVAL_CHARACTERISTIC;
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
        return Uuids.v4.ACCELEROMETER_MODE_CHARACTERISTIC;
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
        return Uuids.v4.IMPACT_THRESHOLD_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getStatusCharacteristic() {
        return Uuids.v4.STATUS_CHARACTERISTIC;
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
        return Uuids.v4.HUMID_TEMP_BUFFER_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getHumidTempBufferSizeCharacteristic() {
        return Uuids.v4.HUMID_TEMP_BUFFER_SIZE_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getImpactBufferCharacteristic() {
        return Uuids.v4.IMPACT_BUFFER_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getImpactBufferSizeCharacteristic() {
        return Uuids.v4.IMPACT_BUFFER_SIZE_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getMotionBufferCharacteristic() {
        return Uuids.v4.MOTION_BUFFER_CHARACTERISTIC;
    }

    @Override
    @NonNull
    @BleCharacteristic
    public UUID getMotionBufferSizeCharacteristic() {
        return Uuids.v4.MOTION_BUFFER_SIZE_CHARACTERISTIC;
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

    @NonNull
    @Override
    @BleService
    public UUID getBeaconModeService() {
        return getBlustreamService();
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getBeaconModeCharacteristic() {
        return Uuids.v4.BEACON_MODE_CHARACTERISTIC;
    }
    
}
