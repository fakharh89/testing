package io.blustream.sulley.routines;

public interface GetOTAUVersionRoutine extends Routine<GetOTAUVersionRoutine.Listener> {

    interface Listener extends Routine.Listener {
        void onOTAUVersionSuccess(Integer otauVersion);

    }
}
