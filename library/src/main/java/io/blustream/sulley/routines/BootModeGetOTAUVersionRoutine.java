package io.blustream.sulley.routines;

public interface BootModeGetOTAUVersionRoutine extends Routine<BootModeGetOTAUVersionRoutine.Listener> {

    interface Listener extends Routine.Listener {
        void onOTAUVersionSuccess(Integer otauVersion);

    }
}
