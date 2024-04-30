package io.blustream.sulley.utilities;

public class HexBytesHelper {
    final private static char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public String reverseBytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            int index = 2 * (bytes.length - 1) - 2 * j;
            hexChars[index] = HEX_ARRAY[v >>> 4];
            hexChars[index + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
