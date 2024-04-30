package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.AccelerometerMode;

import static io.blustream.sulley.models.AccelerometerMode.MOTION;
import static io.blustream.sulley.models.AccelerometerMode.MOTION_AND_IMPACT;
import static io.blustream.sulley.models.AccelerometerMode.OFF;

public class AccelerometerModeMapper implements ByteMapper<AccelerometerMode> {
    @NonNull
    @Override
    public AccelerometerMode fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        AccelerometerMode mode;

        if (bytes[0] == byteForAccelerometerMode(OFF)) {
            mode = OFF;
        } else if (bytes[0] == byteForAccelerometerMode(MOTION)) {
            mode = MOTION;
        } else if (bytes[0] == byteForAccelerometerMode(MOTION_AND_IMPACT)) {
            mode = MOTION_AND_IMPACT;
        } else {
            throw new MapperException.InvalidData();
        }

        return mode;
    }

    @NonNull
    @Override
    public byte[] toBytes(AccelerometerMode mode) throws MapperException {
        byte[] bytes = new byte[expectedWriteLength()];
        bytes[0] = byteForAccelerometerMode(mode);
        return bytes;
    }

    @Override
    public Short expectedReadLength() {
        return 1;
    }

    @Override
    public Short expectedWriteLength() {
        return 1;
    }

    private byte byteForAccelerometerMode(AccelerometerMode mode) throws MapperException.InvalidData {
        checkNotNull(mode);
        byte modeByte;

        switch (mode) {
            case OFF:
                modeByte = 0;
                break;
            case MOTION:
                modeByte = 2; // This value is not a typo
                break;
            case MOTION_AND_IMPACT:
                modeByte = 1; // This value is not a typo
                break;
            default:
                throw new MapperException.InvalidData();  // I think we don't want to let app crash in this case. Think there should be MapperException
        }

        return modeByte;
    }
}
