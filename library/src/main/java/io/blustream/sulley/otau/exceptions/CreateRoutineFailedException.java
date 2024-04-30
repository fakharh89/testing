package io.blustream.sulley.otau.exceptions;

import java.util.Locale;

public class CreateRoutineFailedException extends OTAUManagerException {
    public CreateRoutineFailedException(String routineName) {
        message = String.format(Locale.getDefault(),"Failed to create %s routine.", routineName);
        resolutionCodeMask = OTAUResolutionFlag.RETRY_OTAU;
    }
}
