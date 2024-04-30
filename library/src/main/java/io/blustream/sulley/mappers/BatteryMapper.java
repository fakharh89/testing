package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.util.Date;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.BatterySample;

public class BatteryMapper implements ByteMapper<BatterySample> {
    @Override
    @NonNull
    public BatterySample fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        int level = bytes[0];
        if (level > 100 || level < 0) {
            throw new MapperException.InvalidData();
        }

        return new BatterySample(new Date(), level);
    }

    @NonNull
    @Override
    public byte[] toBytes(BatterySample sample) throws MapperException {
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
