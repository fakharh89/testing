package io.blustream.sulley.mappers;

import java.util.Arrays;
import java.util.Date;

import androidx.annotation.NonNull;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.ImpactSample;

public class ImpactMapper implements ByteMapper<ImpactSample> {
    @Override
    public @NonNull ImpactSample fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        int rawX = (bytes[7] << 8) | (bytes[6] & 0xff);
        int rawY = (bytes[9] << 8) | (bytes[8] & 0xff);
        int rawZ = (bytes[11] << 8) | (bytes[10] & 0xff);

        // 4 milli-G's per LSB
        float x = 31.25f * rawX / 1000f;
        float y = 31.25f * rawY / 1000f;
        float z = 31.25f * rawZ / 1000f;

        DateMapper mapper = new DateMapper();

        Date date = mapper.fromBytes(Arrays.copyOfRange(bytes, 0, 6));

        return new ImpactSample(date, x, y, z);
    }

    @NonNull
    @Override
    public byte[] toBytes(ImpactSample sample) throws MapperException {
        throw new MapperException.NotSensorValid();
    }

    @Override
    public Short expectedReadLength() {
        return 12;
    }

    @Override
    public Short expectedWriteLength() {
        return null;
    }
}
