package io.blustream.sulley.sensor;

import io.blustream.logger.Log;
import io.blustream.sulley.utilities.HexBytesHelper;

public class IBeaconParser {

    private static final String BLUSTREAM_UUID = "8b719ff8-d50e-67b4-e811-0633ccaa3ae9";

    public boolean isBlustreamIBeacon(byte[] scanRecord) {
        Log.d("isIBeacon");
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
            //Convert to hex String
            String uuid = getUUID(scanRecord, startByte);
            return uuid.equalsIgnoreCase(BLUSTREAM_UUID); // ignore if uuid doesn't belongs to Blustream.
        }
        return false;
    }

    private String getUUID(byte[] scanRecord, int startByte) {
        byte[] uuidBytes = new byte[16];
        System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
        String hexString = new HexBytesHelper().bytesToHex(uuidBytes);
        //Here is your UUID
        String uuid = hexString.substring(0, 8) + "-" +
                hexString.substring(8, 12) + "-" +
                hexString.substring(12, 16) + "-" +
                hexString.substring(16, 20) + "-" +
                hexString.substring(20, 32);
        return uuid;
    }
}
