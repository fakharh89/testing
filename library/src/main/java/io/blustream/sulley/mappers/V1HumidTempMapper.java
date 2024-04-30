package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Date;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.HumidTempSample;

public class V1HumidTempMapper implements ByteMapper<HumidTempSample> {
    @NonNull
    @Override
    public HumidTempSample fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);
        // Data format: 0-5 is time in microseconds, 6/7 is humidity (% RH), 8/9 is temp (deg C)
        int rawHumidity = ((int) bytes[7] << 8) | (bytes[6] & 0xff);
        int rawTemperature = (((int) bytes[9]) << 8) | (bytes[8] & 0xff);

        float humidity = rawHumidity / 100f;
        float temperature = rawTemperature / 100f;
        
        // Make sure humidity is ok
        if ((humidity < 0) || (humidity > 100)) {
            throw new MapperException.InvalidData();
        }

        // Make sure temperature is ok
        if ((temperature < -50) || (temperature > 150)) {
            throw new MapperException.InvalidData();
        }

        V1DateMapper mapper = new V1DateMapper();

        Date date = mapper.fromBytes(Arrays.copyOfRange(bytes, 0, 6));

        return new HumidTempSample(date, humidity, temperature);
    }

    @NonNull
    @Override
    public byte[] toBytes(HumidTempSample object) throws MapperException {
        throw new MapperException.NotSensorValid();
    }

    @Override
    public Short expectedReadLength() {
        return 10;
    }

    @Override
    public Short expectedWriteLength() {
        return null;
    }
}
