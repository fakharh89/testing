package io.blustream.sulley.otau.exceptions;

public class FirmwarePropertiesMismatchException extends OTAUManagerException {
    public FirmwarePropertiesMismatchException() {
        message = "Firmware properties did not match after sensor update.";
        resolution_suggestion = "Try updating again, or contact support.";
        resolutionCodeMask = OTAUResolutionFlag.RETRY_OTAU | OTAUResolutionFlag.CONTACT_SUPPORT;
    }
}
