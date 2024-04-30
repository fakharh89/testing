package io.blustream.sulley.models;

import androidx.annotation.NonNull;

import java.util.Date;

public class VisibilityStatus extends AbstractSample {
    public static final int STATE_NOT_VISIBLE = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_ADVERTISED_BLUSTREAM = 1 << 1;
    public static final int STATE_ADVERTISED_IBEACON = 1 << 2;
    public static final int STAT_ADVERTISED_OTAU_BOOT_MODE = 1 << 4;

    private long EVENT_TIMEOUT = 80_000;

    private int mStatus = STATE_NOT_VISIBLE;

    private VisibilityStatus(Date date, int status) {
        super(date);
        this.mStatus = status;
    }

    public VisibilityStatus(int status) {
        this(new Date(), status);
    }

    public VisibilityStatus() {
        this(new Date(), STATE_NOT_VISIBLE);
    }

    public int getStatus() {
        return mStatus;
    }

    private boolean compareBitmask(int b, int flag) {
        return (b & flag) == flag;
    }

    public boolean isConnected() {
        return compareBitmask(mStatus, STATE_CONNECTED);
    }

    public void setConnected(boolean connected) {
        if (connected) {
            mStatus |= STATE_CONNECTED;
        } else if (isIBeaconAdvertised()) {
            mStatus &= ~STATE_CONNECTED;
        } else {
            // do nothing. it is already unset.
        }
        mDate = new Date();
    }

    public boolean isBluestreamAdvertised() {
        return compareBitmask(mStatus, STATE_ADVERTISED_BLUSTREAM);
    }

    public boolean isIBeaconAdvertised() {
        return compareBitmask(mStatus, STATE_ADVERTISED_IBEACON);
    }

    public boolean isOTAUBootModeAdvertised() {
        return compareBitmask(mStatus, STAT_ADVERTISED_OTAU_BOOT_MODE);
    }

    public void setIBeaconAdvertised(boolean advertised) {
        if (advertised) {
            mStatus |= STATE_ADVERTISED_IBEACON;
        } else if (isIBeaconAdvertised()) {
            mStatus &= ~STATE_ADVERTISED_IBEACON;
        } else {
            // do nothing. it is already unset.
        }
        mDate = new Date();
    }

    public void setBlustreamAdvertised(boolean advertised) {
        if (advertised) {
            mStatus |= STATE_ADVERTISED_BLUSTREAM;
        } else if (isBluestreamAdvertised()) {
            mStatus &= ~STATE_ADVERTISED_BLUSTREAM;
        } else {
            // do nothing. it is already unset.
        }
        mDate = new Date();
    }

    public void setOTAUBootModeAdvertised(boolean advertised) {
        if (advertised) {
            mStatus |= STAT_ADVERTISED_OTAU_BOOT_MODE;
        } else if (isOTAUBootModeAdvertised()) {
            mStatus &= ~STAT_ADVERTISED_OTAU_BOOT_MODE;
        }
        mDate = new Date();
    }

    public long getEventTimeout() {
        return EVENT_TIMEOUT;
    }

    public void setEventTimeout(long eventTimeout) {
        this.EVENT_TIMEOUT = eventTimeout;
    }

    public boolean isAdvertising() {
        return (new Date().getTime() - mDate.getTime()) <= EVENT_TIMEOUT;
    }

    @Override
    @NonNull
    public String toString() {
        String binaryString = String.format("%8s", Integer.toBinaryString(mStatus & 0xFF))
                .replace(' ', '0');
        return super.toString() + " Status: 0b" + binaryString;
    }
}
