package io.blustream.sulley.routines;

public interface WriteFirmwareImageRoutine extends Routine<WriteFirmwareImageRoutine.Listener>{
    void setSimulateFailure(boolean simulateFailure);

    interface Listener extends Routine.Listener {
        void onWriteStarted();

        void writeProgress(int percentComplete);

        void onWriteComplete();
    }
}
