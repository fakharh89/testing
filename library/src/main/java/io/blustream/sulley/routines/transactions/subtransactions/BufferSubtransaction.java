package io.blustream.sulley.routines.transactions.subtransactions;

import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import io.blustream.sulley.mappers.BufferMapper;
import io.blustream.sulley.models.Sample;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public interface BufferSubtransaction<T extends Sample> {
    void setListener(BufferSubtransaction.Listener<T> listener);
    BufferSubtransaction.Listener<T> getListener();

    BleDefinitions getDefinitions();

    short getMaxSamples();
    boolean succeedOnDisconnectAfterDelete();

    @NonNull
    BufferMapper<T> getBufferMapper();

    interface BleDefinitions {
        @NonNull
        @BleService
        UUID getBufferService();

        @NonNull
        @BleCharacteristic
        UUID getBufferCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getBufferSizeCharacteristic();
    }

    interface Listener<T extends Sample> {
        void didGetData(List<T> samples);
        void clearedBuffer();
        void didEncounterError(); // TODO figure out how to expose errors
    }
}
