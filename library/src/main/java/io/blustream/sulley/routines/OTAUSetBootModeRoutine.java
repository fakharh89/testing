package io.blustream.sulley.routines;

public interface OTAUSetBootModeRoutine  extends Routine<OTAUSetBootModeRoutine.Listener> {

    interface Listener extends Routine.Listener {
        void onSuccess();
    }
}
