package io.blustream.sulley.mappers;

import java.util.Arrays;
import java.util.Date;

import androidx.annotation.NonNull;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.MotionSample;

public class MotionMapper implements ByteMapper<MotionSample> {
    @Override
    @NonNull
    public MotionSample fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        boolean moving = bytes[6] == 1;

        DateMapper mapper = new DateMapper();

        Date date = mapper.fromBytes(Arrays.copyOfRange(bytes, 0, 6));

        return new MotionSample(date, moving);
    }

    @NonNull
    @Override
    public byte[] toBytes(MotionSample sample) throws MapperException {
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
