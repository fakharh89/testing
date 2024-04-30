package io.blustream.sulley.routines;

import io.blustream.sulley.sensor.Sensor;

public interface Editable<T extends Editable.Listener, U extends Editable.Settings> {
    boolean checkEditableSettings();

    T getEditableListener();
    void setEditableListener(T listener);

    Future<U> getEditableSettingsFuture();
    void setEditableSettingsFuture(Future<U> future);

    interface Future<U extends Editable.Settings> {
        U getEditableSettings(Sensor sensor);
    }

    interface Listener {
        // TODO Add reasons
        void didConfirmEditableSettings(Sensor sensor);
        void didFailConfirmEditableSettings(Sensor sensor);
        // TODO add didFailToGetSettings();
    }

    interface Settings {
        void validate() throws InvalidSettingsException;

        class InvalidSettingsException extends Exception{}
    }
}
