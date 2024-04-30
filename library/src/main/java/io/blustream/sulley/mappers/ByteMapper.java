package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import io.blustream.sulley.mappers.exceptions.MapperException;

public interface ByteMapper<T> {
    @NonNull
    T fromBytes(byte[] bytes) throws MapperException;

    @NonNull
    byte[] toBytes(T object) throws MapperException;

    Short expectedReadLength();

    Short expectedWriteLength();

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

    default void checkWriteLength(byte[] bytes) throws MapperException {
        checkNotNull(bytes);
        if (bytes.length != expectedWriteLength()) {
            throw new MapperException.InvalidLength();
        }
    }
}
