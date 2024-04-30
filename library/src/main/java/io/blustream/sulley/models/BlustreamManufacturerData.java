package io.blustream.sulley.models;

public interface BlustreamManufacturerData extends ManufacturerData {
    String getSerialNumber();
    HumidTempSample getHumidTempSample();
    Status getStatus();
}
