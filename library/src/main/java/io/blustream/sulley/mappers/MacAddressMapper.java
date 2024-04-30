package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.nio.ByteOrder;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.utilities.ByteUtils;

public class MacAddressMapper implements ByteMapper<String> {
    @NonNull
    @Override
    public String fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);
        return ByteUtils.byteArrayToHexString(bytes, ByteOrder.LITTLE_ENDIAN);
    }

    @NonNull
    @Override
    public byte[] toBytes(String object) throws MapperException {
        return new byte[0];
    }

    @Override
    public Short expectedReadLength() {
        return 6;
    }

    @Override
    public Short expectedWriteLength() {
        return null;
    }
}
