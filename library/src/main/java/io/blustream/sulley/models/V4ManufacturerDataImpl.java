package io.blustream.sulley.models;

import java.util.Date;

public class V4ManufacturerDataImpl extends AbstractSample implements V4ManufacturerData {
    private String mSerialNumber;
    private HumidTempSample mHumidTempSample;
    private Status mStatus;

    public V4ManufacturerDataImpl(Date date, String serialNumber, HumidTempSample humidTempSample,
                                  Status status) {
        super(date);
        mSerialNumber = serialNumber;
        mHumidTempSample = humidTempSample;
        mStatus = status;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    public HumidTempSample getHumidTempSample() {
        return mHumidTempSample;
    }

    public Status getStatus() {
        return mStatus;
    }

    // TODO implement toString() override
}
