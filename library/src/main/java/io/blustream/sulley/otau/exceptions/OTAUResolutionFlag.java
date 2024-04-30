package io.blustream.sulley.otau.exceptions;

public class OTAUResolutionFlag {
    public static final int UNKNOWN = 1;
    public static final int RETRY_OTAU = 2;
    public static final int TOGGLE_BLUETOOTH = 4;
    public static final int RESTART_PHONE = 8;
    public static final int RESET_BATTERY = 16;
    public static final int REPLACE_BATTERY = 32;
    public static final int CONTACT_SUPPORT = 64;
}
