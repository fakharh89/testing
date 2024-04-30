package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import io.blustream.sulley.mappers.exceptions.MapperException;

public class BuildIdMapper implements ByteMapper<Integer> {
    @NonNull
    @Override
    public Integer fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);
        return bytes[1] << 8 | bytes[0];
    }

    @NonNull
    @Override
    public byte[] toBytes(Integer object) throws MapperException {
        return new byte[0];
    }

    @Override
    public Short expectedReadLength() {
        return 2;
    }

    @Override
    public Short expectedWriteLength() {
        return null;
    }
}
