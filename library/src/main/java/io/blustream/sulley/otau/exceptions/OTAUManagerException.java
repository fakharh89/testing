package io.blustream.sulley.otau.exceptions;

public class OTAUManagerException extends Exception {
    public OTAUManagerException() {}
    public OTAUManagerException(String msg) { super(msg); }
    public OTAUManagerException(Throwable cause) { super(cause); }
    public OTAUManagerException(String msg, Throwable cause) { super(msg, cause); }

    public String message = "OTAUManager had an unexpected problem";
    public String resolution_suggestion = "Contact support";
    public int resolutionCodeMask = OTAUResolutionFlag.UNKNOWN;

    public final String getMessage() {
        return message;
    }

    public final String getResolutionSuggestion() {
        return resolution_suggestion;
    }

    public final int getResolutionCodeMask() {
        return resolutionCodeMask;
    }
}

