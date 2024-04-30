package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;
import io.blustream.sulley.mappers.exceptions.MapperException;

public class BlinkMapper implements ByteMapper<Integer> {
    @NonNull
    @Override
    public Integer fromBytes(byte[] bytes) throws MapperException {
        throw new MapperException.NotSensorValid();
    }

    @NonNull
    @Override
    public byte[] toBytes(Integer count) throws MapperException {
        if (count == null || count <= 0) {
            throw new MapperException.InvalidData();
        }

        byte[] data = new byte[1];
        data[0] = count.byteValue();
        return data;
    }

    @Override
    public Short expectedReadLength() {
        return null;
    }

    @Override
    public Short expectedWriteLength() {
        return 1;
    }
}
