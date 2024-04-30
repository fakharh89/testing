package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Date;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.MotionSample;

public class V1MotionMapper implements ByteMapper<MotionSample> {
    @NonNull
    @Override
    public MotionSample fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        boolean moving = bytes[6] == 1;

        V1DateMapper mapper = new V1DateMapper();

        Date date = mapper.fromBytes(Arrays.copyOfRange(bytes, 0, 6));

        return new MotionSample(date, moving);
    }

    @NonNull
    @Override
    public byte[] toBytes(MotionSample object) throws MapperException {
        throw new MapperException.NotSensorValid();
    }

    @Override
    public Short expectedReadLength() {
        return 7;
    }

    @Override
    public Short expectedWriteLength() {
        return null;
    }
}
