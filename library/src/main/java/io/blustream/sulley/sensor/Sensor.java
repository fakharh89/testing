package io.blustream.sulley.sensor;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;

import java.io.File;
import java.io.Serializable;

import io.blustream.sulley.models.VisibilityStatus;
import io.blustream.sulley.otau.OTAUManager;
import io.blustream.sulley.routines.Routine;

public interface Sensor extends Serializable {

    @NonNull
    BleDevice getBleDevice();

    BluetoothDevice getNativeDevice(); // TODO Fix location manager in framework to not depend on this

    SensorConnectionState getState();

    VisibilityStatus getVisibilityStatus();

    void setVisibilityStatus(VisibilityStatus visibilityStatus);

    boolean isWithinProximity();

    @NonNull
    String getSerialNumber();

    String getSoftwareVersion();

    String getHardwareVersion();

    int getAdvertisedRssi();

    void setAdvertisedRssi(int rssi);

    SensorLifecycleListener getListener();

    void setListener(SensorLifecycleListener listener);

    boolean connect();

    void disconnect();

    boolean shouldReconnect();

    boolean startRoutine(Routine routine);

    void stopRoutine();

    Routine getRoutine();

    void beginSensorUpgrade(Context context, File imageFile, OTAUManager.Listener listener);

    void cancelSensorUpgrade();

}
