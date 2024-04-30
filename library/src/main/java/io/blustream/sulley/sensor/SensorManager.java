package io.blustream.sulley.sensor;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;

/**
 * SensorManager test doc!
 */
public interface SensorManager {

    /**
     * Starts scanning.
     *
     * @return true if scanning was started, false if not.
     */

    boolean startAdvScanning();

    boolean startProxScanning(Set<String> sensorsMac);

    void stopAdvScanning();

    void stopProxScanning();

    boolean isAdvScanning();

    boolean isProxScanning();

    SensorCache getSensorCache();

    @Nullable
    UhOhRemedyListener getUhOhRemedyListener();

    void setUhOhRemedyListener(@Nullable UhOhRemedyListener listener);

    SensorLifecycleListener getAdvLifecycleListener();

    void setLifecycleListener(SensorLifecycleListener listener);

    @NonNull
    Sensor getSensorFromSerialNumber(String serialNumber);

    @Deprecated //we cant setup permissions inside a library. Client app should do this.
    void setupPermissions(Activity activity, SetupPermissionsCallback callback);

    @Deprecated
    interface SetupPermissionsCallback {
        void onSuccess();

        void onFailure(); // TODO Expose error
    }

    // TODO add Phone's BLE state and possibly move SetupPermissionsCallback into that
    // TODO make sure delayed location permission request works
    // TODO Add UhOh listeners
    // TODO Expand permission helper
}
