package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import io.blustream.sulley.mappers.exceptions.MapperException;

public class RegistrationMapper implements ByteMapper<Integer> {

    @NonNull
    @Override
    public Integer fromBytes(byte[] bytes) throws MapperException {
        checkNotNull(bytes);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getInt();
    }

    @NonNull
    @Override
    public byte[] toBytes(Integer integer) throws MapperException {
        checkNotNull(integer);
        ByteBuffer byteBuffer = ByteBuffer.allocate(expectedWriteLength());
        byteBuffer.putInt(integer);

        return byteBuffer.array();
    }

    @Override
    public Short expectedReadLength() {
        return null; // todo: add array length to avoid potential problems with wrong size arrays.
    }

    @Override
    public Short expectedWriteLength() {
        return 4;
    }
}
