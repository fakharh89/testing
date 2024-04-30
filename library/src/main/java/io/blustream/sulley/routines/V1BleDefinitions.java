package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.blustream.sulley.utilities.Uuids;

public class V1BleDefinitions implements V1Routine.BleDefinitions {
    @NonNull
    @BleService
    private UUID getBlustreamService() {
        return Uuids.v1.BLUSTREAM_SERVICE;
    }

    @NonNull
    @Override
    @BleService
    public UUID getDataService() {
        return getBlustreamService();
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getHumidTempDataCharacteristic() {
        return Uuids.v1.HUMID_TEMP_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getImpactDataCharacteristic() {
        return Uuids.v1.IMPACT_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getMotionDataCharacteristic() {
        return Uuids.v1.MOTION_CHARACTERISTIC;
    }

    @NonNull
    @Override
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
    public UUID getStatusService() {
        return getBlustreamService();
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getStatusCharacteristic() {
        return Uuids.v1.STATUS_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getBlinkService() {
        return getBlustreamService();
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getBlinkCharacteristic() {
        return Uuids.v1.BLINK_CHARACTERISTIC;
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
        return Uuids.v1.REALTIME_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getAccelerometerModeService() {
        return getBlustreamService();
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getAccelerometerModeCharacteristic() {
        return Uuids.v1.ACCELEROMETER_MODE_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getHumidTempSamplingIntervalService() {
        return getBlustreamService();
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getHumidTempSamplingIntervalCharacteristic() {
        return Uuids.v1.HUMID_TEMP_SAMPLING_INTERVAL_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getImpactThresholdService() {
        return getBlustreamService();
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getImpactThresholdCharacteristic() {
        return Uuids.v1.IMPACT_THRESHOLD_CHARACTERISTIC;
    }

    @NonNull
    @Override
    @BleService
    public UUID getRegistrationService() {
        return getBlustreamService();
    }

    @NonNull
    @Override
    @BleCharacteristic
    public UUID getRegistrationCharacteristic() {
        return Uuids.v1.REGISTRATION_CHARACTERISTIC;
    }
}
