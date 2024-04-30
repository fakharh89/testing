package io.blustream.sulley.otau.model;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.Objects;

public class FirmwareProperties {

    private String mMacAddress;
    private int mCrystalTrim;
    private String mUserKeys;

    public FirmwareProperties() {
    }

    public FirmwareProperties(String mMacAddress) {
        this.mMacAddress = mMacAddress;
    }

    public String getMacAddress() {
        return mMacAddress;
    }

    public void setMacAddress(@NonNull String macAddress) {
        this.mMacAddress = macAddress;
    }

    public void setCrystalTrim(int crystalTrim) {
        this.mCrystalTrim = crystalTrim;
    }

    public void setUserKeys(String userKeys) {
        this.mUserKeys = userKeys;
    }

    public int getCrystalTrim() {
        return mCrystalTrim;
    }

    public String getUserKeys() {
        return mUserKeys;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%1$s;%2$d;%3$s", mMacAddress, mCrystalTrim, mUserKeys);
    }

    public static FirmwareProperties fromString(String cacheString) {
        if (cacheString == null) {
            return null;
        }
        String[] result = cacheString.split(";");
        String macAddress = result[0];
        int crystalTrim = Integer.parseInt(result[1]);
        String userKeys = result[2];

        FirmwareProperties properties = new FirmwareProperties();
        properties.setMacAddress(macAddress);
        properties.setCrystalTrim(crystalTrim);
        properties.setUserKeys(userKeys);

        return properties;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FirmwareProperties)) {
            return false;
        }

        FirmwareProperties other = (FirmwareProperties) o;
        return getMacAddress().equalsIgnoreCase(other.getMacAddress()) &&
                getCrystalTrim() == other.getCrystalTrim() &&
                getUserKeys().equalsIgnoreCase(other.getUserKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMacAddress(), getCrystalTrim(), getUserKeys());
    }
}
