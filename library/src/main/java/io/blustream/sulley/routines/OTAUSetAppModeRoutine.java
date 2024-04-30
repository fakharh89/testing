package io.blustream.sulley.routines;

public interface OTAUSetAppModeRoutine  extends Routine<OTAUSetAppModeRoutine.Listener> {

    interface Listener extends Routine.Listener {
        void onSuccess();
    }
}
