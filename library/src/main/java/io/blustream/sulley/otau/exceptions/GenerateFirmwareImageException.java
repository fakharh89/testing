package io.blustream.sulley.otau.exceptions;

public class GenerateFirmwareImageException extends OTAUManagerException {
    public GenerateFirmwareImageException() {
        message = "Failed to generate firmware image";
        resolutionCodeMask = OTAUResolutionFlag.RETRY_OTAU;
    }
}
