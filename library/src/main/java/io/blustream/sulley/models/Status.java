package io.blustream.sulley.models;

import java.util.Date;

public class Status extends AbstractSample {
    // Normally these would be bytes (or chars), but java doesn't have unsigned types
    private static final int HUMID_TEMP_SENSOR_ERROR = 1;
    private static final int ACCEL_SENSOR_ERROR = 1 << 1;
    private static final int TEMP_SENSOR_2_ERROR = 1 << 2;
    private static final int TEMP_SENSOR_3_ERROR = 1 << 3;
    private static final int CHIP_RESET = 1 << 4;
    private static final int FIFO_ERROR = 1 << 5;
    private static final int REG_TIMEOUT = 1 << 6;
    private static final int UNREAD_DATA = 1 << 7;

    private Byte mStatus;

    public Status(Date date, Byte status) {
        super(date);

        mStatus = status;
    }

    public Byte getStatus() {
        return mStatus;
    }

    @Override
    public String toString() {
        String binaryString = String.format("%8s", Integer.toBinaryString(mStatus & 0xFF))
                .replace(' ', '0');
        return super.toString() + " Status: 0b" + binaryString;
    }

    public boolean hasHumidTempSensorError() {
        return compareBitmask(mStatus, HUMID_TEMP_SENSOR_ERROR);
    }

    public boolean hasAccelSensorError() {
        return compareBitmask(mStatus, ACCEL_SENSOR_ERROR);
    }

    public boolean hasTempSensor2Error() {
        return compareBitmask(mStatus, TEMP_SENSOR_2_ERROR);
    }

    public boolean hasTempSensor3Error() {
        return compareBitmask(mStatus, TEMP_SENSOR_3_ERROR);
    }

    public boolean didChipReset() {
        return compareBitmask(mStatus, CHIP_RESET);
    }

    public boolean hasFIFOError() {
        return compareBitmask(mStatus, FIFO_ERROR);
    }

    public boolean didRegistrationTimeout() {
        return compareBitmask(mStatus, REG_TIMEOUT);
    }

    public boolean hasUnreadData() {
        return compareBitmask(mStatus, UNREAD_DATA);
    }

    private boolean compareBitmask(byte b, int flag) {
        return (b & flag) == flag;
    }
}
