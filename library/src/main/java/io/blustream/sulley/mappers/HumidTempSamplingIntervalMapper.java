package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.blustream.sulley.mappers.exceptions.MapperException;

public class HumidTempSamplingIntervalMapper implements ByteMapper<Integer> {
    @NonNull
    @Override
    public Integer fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        int interval = 0;

        for (int i = 3; i >= 0; i--) {
            interval = interval << 8;
            interval |= bytes[i] & 0xff;
        }

        return interval;
    }

    @NonNull
    @Override
    public byte[] toBytes(Integer interval) throws MapperException {
        checkNotNull(interval);
        ByteBuffer b = ByteBuffer.allocate(4);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(interval);
        return b.array();
    }

    @Override
    public Short expectedReadLength() {
        return 4;
    }

    @Override
    public Short expectedWriteLength() {
        return 4;
    }
}
