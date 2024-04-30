package io.blustream.sulley.sensor;

import android.bluetooth.le.ScanResult;
import android.util.SparseArray;

import io.blustream.logger.Log;
import io.blustream.sulley.utilities.MacAddressHelper;

public class ScanUtils {

    public static String decodeSerialFromIBeacon(byte[] scanRecord) {
        if (scanRecord == null || scanRecord.length <= 0) {
            return null;
        }
        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }
        if (patternFound) {
            //Here is your Major value
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            //Here is your Minor value
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);
            return covertMajorMinorToSerial(minor, major);
        }
        return null;
    }

    private static String covertMajorMinorToSerial(int minor, int major) {
        StringBuilder serial = new StringBuilder();
        String minorString = fromIntToHex(minor);
        String majorString = fromIntToHex(major);

        serial.append(minorString.substring(2))
                .append(minorString.substring(0, 2))
                .append(majorString.substring(2))
                .append(majorString.substring(0, 2));
        return serial.toString();
    }

    private static String fromIntToHex(int val) {
        String res = Integer.toHexString(val);
        if (res.length() < 4) {
            int diff = 4 - res.length();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < diff; i++) {
                stringBuilder.append("0");
            }
            res = stringBuilder.append(res).toString();
        }
        return res;
    }

    /**
     * Used to determine if a scanned device is compatible with Blustream
     * Uses the ScanResult object to make the determination
     *
     * @param scanResult
     * @return true if the device associated with scanResult is compatible, false if not.
     */
    public static boolean isBlustreamCompatible(ScanResult scanResult) {
        boolean result = false;

        MacAddressHelper macAddressHelper = new MacAddressHelper();
        if (macAddressHelper.isBlustreamMacAddress(scanResult.getDevice().getAddress())) {
            result = true;
        }

        return result;
    }

    public static byte[] getMsdBytes(ScanResult scanResult) {
        if (scanResult.getScanRecord() == null) {
            Log.d("scanResult.getScanRecord() == null. returned.");
            return null;
        }
        SparseArray<byte[]> msd = scanResult.getScanRecord().getManufacturerSpecificData();
        if (msd.size() == 0) {
            //Log.d("msd.size() == 0. returned.");
            return null;
        }
        int key = msd.keyAt(0);
        byte[] bytes = msd.get(key);
        if (bytes != null) {
            return bytes;
        }
        return null;
    }
}
