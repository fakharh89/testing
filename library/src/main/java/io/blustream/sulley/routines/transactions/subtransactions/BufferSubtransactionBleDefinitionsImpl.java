package io.blustream.sulley.routines.transactions.subtransactions;

import java.util.UUID;

import androidx.annotation.NonNull;

public class BufferSubtransactionBleDefinitionsImpl implements BufferSubtransaction.BleDefinitions {
    private UUID mBufferService;
    private UUID mBufferCharacteristic;
    private UUID mBufferSizeCharacteristic;

    public BufferSubtransactionBleDefinitionsImpl(@NonNull UUID bufferService,
                                                  @NonNull UUID bufferCharacteristic,
                                                  @NonNull UUID bufferSizeCharacteristic) {
        mBufferService = bufferService;
        mBufferCharacteristic = bufferCharacteristic;
        mBufferSizeCharacteristic = bufferSizeCharacteristic;
    }

    @Override
    @NonNull
    public UUID getBufferService() { return mBufferService; }

    @Override
    @NonNull
    public UUID getBufferCharacteristic() {
        return mBufferCharacteristic;
    }

    @Override
    @NonNull
    public UUID getBufferSizeCharacteristic() {
        return mBufferSizeCharacteristic;
    }
}
