package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.UUID;

import io.blustream.sulley.models.HumidTempSample;
import io.blustream.sulley.models.ImpactSample;
import io.blustream.sulley.models.MotionSample;

interface V1DataSubroutine extends Routine<V1DataSubroutine.Listener> {
    interface BleDefinitions {
        @NonNull
        @BleService
        UUID getDataService();

        @NonNull
        @BleCharacteristic
        UUID getHumidTempDataCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getImpactDataCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getMotionDataCharacteristic();
    }

    interface Listener extends Routine.Listener {
        void didGetHumidTempSamples(List<HumidTempSample> humidTempSamples);
        void didGetImpactSamples(List<ImpactSample> impactSamples);
        void didGetMotionSamples(List<MotionSample> motionSamples);
    }
}
