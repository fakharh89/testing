package io.blustream.sulley.otau.exceptions;

import java.util.Locale;

public class RoutineExecutionFailedException extends OTAUManagerException {
    public RoutineExecutionFailedException(String routineName) {
        message = String.format(Locale.getDefault(), "Execution of %S failed.", routineName);
        resolutionCodeMask = OTAUResolutionFlag.RETRY_OTAU;
    }
}
