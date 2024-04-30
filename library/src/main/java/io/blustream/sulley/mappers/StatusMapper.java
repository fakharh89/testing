package io.blustream.sulley.mappers;

import java.util.Date;

import androidx.annotation.NonNull;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.Status;

public class StatusMapper implements ByteMapper<Status> {
    @NonNull
    @Override
    public Status fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        return new Status(new Date(), bytes[0]);
    }

    @NonNull
    @Override
    public byte[] toBytes(Status status) throws MapperException {
        throw new MapperException.NotSensorValid();
    }

    @Override
    public Short expectedReadLength() {
        return 1;
    }

    @Override
    public Short expectedWriteLength() {
        return null;
    }
}
