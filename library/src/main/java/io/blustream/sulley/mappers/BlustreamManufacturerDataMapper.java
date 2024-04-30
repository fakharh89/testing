package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.util.Date;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.BlustreamManufacturerData;

public class BlustreamManufacturerDataMapper implements
        ManufacturerDataByteMapper<BlustreamManufacturerData> {
    @NonNull
    @Override
    public BlustreamManufacturerData fromBytes(Date date, byte[] bytes) throws MapperException {
        V3ManufacturerDataMapper v3ManufacturerDataMapper = new V3ManufacturerDataMapper();
        V4ManufacturerDataMapper v4ManufacturerDataMapper = new V4ManufacturerDataMapper();
        if (bytes != null) {
            if (bytes.length == v3ManufacturerDataMapper.expectedReadLength()) {
                return v3ManufacturerDataMapper.fromBytes(date, bytes);
            }
            if (bytes.length == v4ManufacturerDataMapper.expectedReadLength()) {
                return v4ManufacturerDataMapper.fromBytes(date, bytes);
            }
        }
        throw new MapperException.InvalidLength();
    }

    @Override
    public Short expectedReadLength() {
        return null;
    }
}
