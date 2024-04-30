package io.blustream.sulley.routines.transactions;

import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import io.blustream.sulley.models.HumidTempSample;
import io.blustream.sulley.models.ImpactSample;
import io.blustream.sulley.models.MotionSample;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public interface BufferDumpTransaction {
    interface Listener {
        void didGetHumidTempSamples(List<HumidTempSample> humidTempSamples);

        void didGetImpactSamples(List<ImpactSample> impactSamples);

        void didGetMotionSamples(List<MotionSample> motionSamples);

        void didClearHumidTempBuffer();

        void didClearImpactBuffer();

        void didClearMotionBuffer();
    }

    interface BleDefinitions {
        @NonNull
        @BleService
        UUID getBufferService();

        @NonNull
        @BleCharacteristic
        UUID getHumidTempBufferCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getHumidTempBufferSizeCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getImpactBufferCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getImpactBufferSizeCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getMotionBufferCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getMotionBufferSizeCharacteristic();
    }
}
