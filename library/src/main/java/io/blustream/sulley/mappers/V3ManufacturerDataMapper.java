package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.util.Date;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.BatterySample;
import io.blustream.sulley.models.HumidTempSample;
import io.blustream.sulley.models.Status;
import io.blustream.sulley.models.V3ManufacturerData;
import io.blustream.sulley.models.V3ManufacturerDataImpl;
import io.blustream.sulley.utilities.HexBytesHelper;

public class V3ManufacturerDataMapper implements ManufacturerDataByteMapper<V3ManufacturerData> {
    @Override
    @NonNull
    public V3ManufacturerData fromBytes(Date date, byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        // TODO Combine and chain several of these mappers

        byte[] snBytes = new byte[4];
        snBytes[0] = bytes[3];
        snBytes[1] = bytes[2];
        snBytes[2] = bytes[1];
        snBytes[3] = bytes[0];

        HexBytesHelper helper = new HexBytesHelper();

        String serialNumber = helper.bytesToHex(snBytes);

        long humidity = ((int) bytes[5] << 8) + (bytes[4] & 0xff);
        float fHumidity = (float) humidity / 100.0f;

        if (fHumidity < 0) {
            fHumidity = 0;
        } else if (fHumidity > 100) {
            fHumidity = 100;
        }

        long temperature = ((int) bytes[7] << 8) + (bytes[6] & 0xff);
        float fTemperature = (float) temperature / 100.0f;

        int battery = bytes[8];
        byte error = bytes[9];

        // TODO Finish these mappers
        return new V3ManufacturerDataImpl(date, serialNumber,
                new HumidTempSample(date, fHumidity, fTemperature),
                new BatterySample(date, battery), new Status(date, error));
    }

    @Override
    public Short expectedReadLength() {
        return 10;
    }
}
