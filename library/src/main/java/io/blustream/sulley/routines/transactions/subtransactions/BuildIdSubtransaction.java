package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.blustream.sulley.mappers.BuildIdMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;

public class BuildIdSubtransaction extends WriteNotifyResponseSubtransaction {
    private static final int BUILD_ID_REQUEST = 0x20000;
    private static final int REQUEST_DATA_LENGTH = 4;

    public BuildIdSubtransaction(@NonNull BleDefinitions definitions) {
        super(definitions);
    }

    @Override
    public byte[] getWriteValue() {
        return(ByteBuffer.allocate(REQUEST_DATA_LENGTH)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(BUILD_ID_REQUEST)
                .array());
    }

    public Integer getBuildId() throws MapperException {
        BuildIdMapper mapper = new BuildIdMapper();
        return mapper.fromBytes(getResponseValue());
    }
}
