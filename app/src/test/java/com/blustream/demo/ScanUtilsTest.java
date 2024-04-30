package com.blustream.demo;

import org.junit.Test;

import io.blustream.sulley.sensor.ScanUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ScanUtilsTest {
    private static String VALID_SERIAL = "00f44902";
    private static String INVALID_SERIAL = "00f44901";
    private static byte[] VALID_SCAN_BYTES = new byte[]{2, -1, -6, 26, -1, -76, 0, 2, 21, -117, 113, -97, -8, -43, 14, 103, -76, -24, 17, 6, 51, -52, -86, 58, -23, 2, 73, -12, 0, -2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //  mock data.

    @Test
    public void testDecodeSerialFromIBeaconPositive() {
        String result = ScanUtils.decodeSerialFromIBeacon(VALID_SCAN_BYTES);
        assertEquals(result, VALID_SERIAL);
    }

    @Test
    public void testDecodeSerialFromIBeaconNegative() {
        String result = ScanUtils.decodeSerialFromIBeacon(VALID_SCAN_BYTES);
        assertNotEquals(result, INVALID_SERIAL);
    }

    @Test
    public void testDecodeSerialFromIBeaconNull() {
        String result = ScanUtils.decodeSerialFromIBeacon(null);
        assertNotEquals(result, VALID_SERIAL);
    }

    @Test
    public void testDecodeSerialFromIBeaconEmptyArray() {
        String result = ScanUtils.decodeSerialFromIBeacon(new byte[0]);
        assertNotEquals(result, VALID_SERIAL);
    }
}
