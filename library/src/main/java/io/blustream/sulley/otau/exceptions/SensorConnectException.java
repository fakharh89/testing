package io.blustream.sulley.otau.exceptions;


public class SensorConnectException extends OTAUManagerException {
    public SensorConnectException() {
        message = "Failed to connect to sensor";
        resolution_suggestion = "Reset battery and try again.";
        resolutionCodeMask = OTAUResolutionFlag.RESET_BATTERY;
    }
}
