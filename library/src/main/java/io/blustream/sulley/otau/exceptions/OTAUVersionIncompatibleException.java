package io.blustream.sulley.otau.exceptions;

import java.util.Locale;

public class OTAUVersionIncompatibleException extends OTAUManagerException {
    public OTAUVersionIncompatibleException(int existingVersion) {
        message = String.format(Locale.getDefault(),
                "OTAU Version: %d is not compatible. Need V6",
                existingVersion);
        resolutionCodeMask = OTAUResolutionFlag.CONTACT_SUPPORT;
    }
}
