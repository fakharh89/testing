package io.blustream.sulley.mappers;

import java.util.List;

import androidx.annotation.NonNull;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.Sample;

public interface BufferMapper<T extends Sample> {
    @NonNull
    List<T> fromBytes(byte[] bytes) throws MapperException;

    @NonNull
    ByteMapper<T> getSubsampleByteMapper();
}
