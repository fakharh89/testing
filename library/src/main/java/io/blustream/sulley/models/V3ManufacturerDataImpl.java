package io.blustream.sulley.models;

import java.util.Date;

public class V3ManufacturerDataImpl extends AbstractSample implements V3ManufacturerData {
    private String mSerialNumber;
    private HumidTempSample mHumidTempSample;
    private BatterySample mBatterySample;
    private Status mStatus;

    public V3ManufacturerDataImpl(Date date, String serialNumber, HumidTempSample humidTempSample,
                                  BatterySample batterySample, Status status) {
        super(date);
        mSerialNumber = serialNumber;
        mHumidTempSample = humidTempSample;
        mBatterySample = batterySample;
        mStatus = status;
    }

    @Override
    public String getSerialNumber() {
        return mSerialNumber;
    }

    @Override
    public HumidTempSample getHumidTempSample() {
        return mHumidTempSample;
    }

    @Override
    public BatterySample getBatterySample() {
        return mBatterySample;
    }

    @Override
    public Status getStatus() {
        return mStatus;
    }

    // TODO implement toString() override
}
