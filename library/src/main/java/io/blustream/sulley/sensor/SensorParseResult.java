package io.blustream.sulley.sensor;

public class SensorParseResult {
    public static enum SensorMode {
        UNKNOWN,
        OTAU,
        IBEACON,
        V1,
        V3,
        V4
    }

    //populate fields in constructor, make final.
    private final SensorMode mSensorMode;
    private final String mSerialNumber;
    private final String mMacAddress;

    SensorParseResult(SensorParseResult.SensorMode mode, String macAddress, String serialNumber) {
        mSensorMode = mode;
        mMacAddress = macAddress;
        mSerialNumber = serialNumber;
    }

    public SensorMode getSensorMode() {
        return mSensorMode;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    public String getMacAddress() {
        return mMacAddress;
    }

}
