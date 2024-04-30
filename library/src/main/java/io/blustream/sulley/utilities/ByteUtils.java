package io.blustream.sulley.utilities;

import java.nio.ByteOrder;

public class ByteUtils {

    private ByteUtils() {
    }

    public static String byteArrayToHexString(byte[] bytes, ByteOrder byteOrder) {
        StringBuilder stringBuilder = new StringBuilder();
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            for (int i = bytes.length - 1; i >= 0; i--) {
                append(stringBuilder, bytes[i]);
            }
        } else {
            for (byte b : bytes) {
                append(stringBuilder, b);
            }
        }

        return stringBuilder.toString();
    }

    private static void append(StringBuilder target, byte b) {
        if (target.length() > 0) {
            target.append(':');
        }
        target.append(String.format("%02X", b));
    }
}
