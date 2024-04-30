package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.util.Date;

import io.blustream.sulley.mappers.exceptions.MapperException;

public interface ManufacturerDataByteMapper<T> {
    @NonNull
    T fromBytes(Date date, byte[] bytes) throws MapperException;

    Short expectedReadLength();

    default void checkNotNull(Object object) throws MapperException.InvalidData {
        if (object == null) {
            throw new MapperException.InvalidData();
        }
    }

    default void checkReadLength(byte[] bytes) throws MapperException {
        checkNotNull(bytes);
        if (bytes.length != expectedReadLength()) {
            throw new MapperException.InvalidLength();
        }
    }
}
