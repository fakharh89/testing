package io.blustream.sulley.sensor;

import java.util.ArrayList;
import java.util.List;

public class StateProvider {

    private List<Listener> listeners = new ArrayList<>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void toBackgroundState() {
        for (Listener listener : listeners) {
            listener.toBackgroundState();
        }
    }

    public void toForegroundState() {
        for (Listener listener : listeners) {
            listener.toForegroundState();
        }
    }

    public interface Listener {

        void toForegroundState();

        void toBackgroundState();
    }
}
