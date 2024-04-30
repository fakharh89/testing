package io.blustream.sulley.sensor;

/**
 * Scan modes for {@link io.blustream.sulley.sensor.AlternativeScanner}
 */
public enum ScanMode {
    /*
     * handles advertisement events only. used only for initial scanning. Works only in foreground.
     */
    ADV,
    /*
     * handles advertisement events, iBeacons, connection and data receiving events.
     * Used in foreground service to monitor proximity . Works both in foreground and background.
     */
    PROXIMITY
}
