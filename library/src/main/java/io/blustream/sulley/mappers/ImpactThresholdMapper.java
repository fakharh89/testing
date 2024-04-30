package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.blustream.sulley.mappers.exceptions.MapperException;

public class ImpactThresholdMapper implements ByteMapper<Float> {
    @NonNull
    @Override
    public Float fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);
        // todo add threshold check like in toBytes();
        return (bytes[0] & 0xff) * 0.0625f;
    }

    @NonNull
    @Override
    public byte[] toBytes(Float threshold) throws MapperException {
        checkNotNull(threshold);
        ByteBuffer b = ByteBuffer.allocate(2);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putChar((char) (threshold / 0.0625)); // TODO Check rounding here

        if ((threshold >= 16) || (threshold <= 0) || (b.getChar(0) >= 0xff)) {
            throw new MapperException.InvalidData();
        }

        return b.array();
    }

    @Override
    public Short expectedReadLength() {
        return 1;
    }

    @Override
    public Short expectedWriteLength() {
        return 2;
    }
}
