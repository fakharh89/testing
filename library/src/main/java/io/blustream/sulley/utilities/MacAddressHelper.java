package io.blustream.sulley.utilities;

public class MacAddressHelper {
    private static final String BLUSTREAM_MAC_PREFIX = "0C:1A:10:";

    public boolean isValidMacAddress(String macAddress) {
        if (macAddress.length() != 17) {
            return false;
        }

        return true;
    }

    public boolean isBlustreamMacAddress(String macAddress) {
        if (!isValidMacAddress(macAddress)) {
            return false;
        }

        if (!macAddress.startsWith(BLUSTREAM_MAC_PREFIX)) {
            return false;
        }

        return true;
    }

    public boolean macMatchesSerialNumber(String macAddress, String serialNumber) {
        return getMacFromSerialNumber(serialNumber).equalsIgnoreCase(macAddress);
    }

    private String getSerialNumberWithoutDeviceCodeFromMac(String macAddress) {
        String serialNumberNoCode = "";

        serialNumberNoCode = serialNumberNoCode.concat(macAddress.substring(9, 11).toUpperCase());
        serialNumberNoCode = serialNumberNoCode.concat(macAddress.substring(12, 14).toUpperCase());
        serialNumberNoCode = serialNumberNoCode.concat(macAddress.substring(15, 17).toUpperCase());

        return serialNumberNoCode;
    }

    public String getSerialNumberFromMac(String macAddress, String deviceCode) {
        return getSerialNumberWithoutDeviceCodeFromMac(macAddress).concat(deviceCode);
    }

    public String getMacFromSerialNumber(String serialNumber) {
        String macAddress = BLUSTREAM_MAC_PREFIX;

        if (serialNumber.length() !=  8) {
            return null;
        }

        macAddress = macAddress.concat(serialNumber.substring(0, 2).toUpperCase().concat(":"));
        macAddress = macAddress.concat(serialNumber.substring(2, 4).toUpperCase().concat(":"));
        macAddress = macAddress.concat(serialNumber.substring(4, 6).toUpperCase());

        return macAddress;
    }
}
