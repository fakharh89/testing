package io.blustream.sulley.otau.exceptions;

public class PSKeyDatabaseException extends OTAUManagerException {
    public PSKeyDatabaseException() {
        message = "Failed to load PSKey database.";
        resolutionCodeMask = OTAUResolutionFlag.RETRY_OTAU | OTAUResolutionFlag.CONTACT_SUPPORT;
    }
}
