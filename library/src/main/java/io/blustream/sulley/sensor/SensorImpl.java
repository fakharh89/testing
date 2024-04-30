package io.blustream.sulley.sensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;

import java.io.File;
import java.util.Objects;

import io.blustream.logger.Log;
import io.blustream.sulley.models.VisibilityStatus;
import io.blustream.sulley.otau.OTAUManager;
import io.blustream.sulley.otau.OTAUManagerImpl;
import io.blustream.sulley.routines.Routine;
import io.blustream.sulley.routines.transactions.GetVersionInitTransaction;

public class SensorImpl implements Sensor {
    private final BleDevice mBleDevice;
    private final GetVersionInitTransaction mInitTransaction = new GetVersionInitTransaction();
    private String mSerialNumber;
    private String softwareVersion;
    private String hardwareVersion;
    private Routine mRoutine;
    private SensorLifecycleListener mListener;
    private boolean mShouldReconnect = false;
    private int advertisedRssi;
    private VisibilityStatus visibilityStatus = new VisibilityStatus(VisibilityStatus.STATE_NOT_VISIBLE);
    OTAUManager otauManager;

    SensorImpl(@NonNull String serialNumber, @NonNull BleDevice device) {
        mSerialNumber = serialNumber;
        mBleDevice = device;
    }

    @NonNull
    @Override
    public BleDevice getBleDevice() {
        return mBleDevice;
    }

    @Override
    public BluetoothDevice getNativeDevice() {
        return mBleDevice.getNative();
    }

    @Override
    public SensorConnectionState getState() {
        return SensorConnectionState.getState(mBleDevice);
    }

    @Override
    public VisibilityStatus getVisibilityStatus() {
        if (visibilityStatus == null) {
            visibilityStatus = new VisibilityStatus();
        }
        return visibilityStatus;
    }

    @Override
    public void setVisibilityStatus(VisibilityStatus visibilityStatus) {
        this.visibilityStatus = visibilityStatus;
    }

    @Override
    public boolean isWithinProximity() {
        return getState() == SensorConnectionState.CONNECTED || getVisibilityStatus().isAdvertising();
    }

    @Override
    @NonNull
    public String getSerialNumber() {
        return mSerialNumber;
    }

    @Override
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    @Override
    public String getHardwareVersion() {
        return hardwareVersion;
    }

    @Override
    public int getAdvertisedRssi() {
        return advertisedRssi;
    }

    @Override
    public void setAdvertisedRssi(int rssi) {
        advertisedRssi = rssi;
    }

    @Override
    public SensorLifecycleListener getListener() {
        return mListener;
    }

    @Override
    public void setListener(SensorLifecycleListener listener) {
        mListener = listener;

        mBleDevice.setListener_State(new SensorLifecycleListenerAdapter(this, listener) {
            @Override
            public void onEvent(BleDevice.StateListener.StateEvent stateEvent) {
                if (stateEvent.didEnter(BleDeviceState.INITIALIZED)) {
                    softwareVersion = mInitTransaction.getSoftwareVersion();
                    hardwareVersion = mInitTransaction.getHardwareVersion();
                }
                super.onEvent(stateEvent);
            }
        });
    }

    @Override
    public boolean connect() {
        mShouldReconnect = true;

//        if (getVisibilityStatus().isOTAUBootModeAdvertised()) {
//            return connectBootloaderMode();
//        }

        BleDevice.ConnectionFailListener.ConnectionFailEvent failEvent = mBleDevice.connect(mInitTransaction, null, e -> {
            if (e.status() != BleDevice.ConnectionFailListener.Status.ALREADY_CONNECTING_OR_CONNECTED) {
                Log.e(getSerialNumber() + " Connection failed. Stack Trace:\n" + android.util.Log.getStackTraceString(new Throwable()));
            }
            return null;
        });

        boolean success = failEvent.isNull();

        if (success) {
            Log.i(getSerialNumber() + " Connecting to sensor");
        } else {
            if (failEvent.status() != BleDevice.ConnectionFailListener.Status.ALREADY_CONNECTING_OR_CONNECTED) {
                Log.e(getSerialNumber() + " Critical! - Connect command failed! \n" + failEvent);
            }
        }

        return true;
    }

    @Override
    public void disconnect() {
        mShouldReconnect = false;

        if (getBleDevice().disconnect()) {
            Log.i(getSerialNumber() + " Disconnecting from sensor");
        } else {
            Log.e(getSerialNumber() + " Disconnect command failed!");
        }
    }

    @Override
    public boolean shouldReconnect() {
        return mShouldReconnect;
    }

    @Override
    public boolean startRoutine(Routine routine) {
        if (mRoutine != null) {
            Log.e(getSerialNumber() + " Failed to start routine - null routine!");
            return false;
        }

        boolean success = routine.start();

        if (success) {
            Log.i(getSerialNumber() + " Starting routine " + routine.getClass().getName());
            mRoutine = routine;
        } else {
            Log.e(getSerialNumber() + " Critical! - Failed to start routine!");
        }

        return success;
    }

    @Override
    public void stopRoutine() {
        if (mRoutine != null) {
            Log.i(getSerialNumber() + " Stopping routine");
            mRoutine.stop();
            mRoutine = null;
        } else {
            Log.e(getSerialNumber() + " Failed to stop routine (doesn't exist)!");
        }
    }

    @Override
    public Routine getRoutine() {
        return mRoutine;
    }

    @Override
    public void beginSensorUpgrade(Context context, File imageFile, OTAUManager.Listener listener) {
        otauManager = new OTAUManagerImpl(context);
        otauManager.setForceFailAtState(null);
        otauManager.setDebugMode(false);
        otauManager.upgradeSensor(this, imageFile, listener);
    }

    @Override
    public void cancelSensorUpgrade() {
        if (otauManager != null) {
            otauManager.stopUpgrade();
            otauManager = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SensorImpl)) return false;
        SensorImpl sensor = (SensorImpl) o;
        return Objects.equals(mSerialNumber, sensor.mSerialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mSerialNumber);
    }

    @NonNull
    @Override
    public String toString() {
        return "SensorImpl{" +
                "mBleDevice=" + mBleDevice +
                ", mInitTransaction=" + mInitTransaction +
                ", mSerialNumber='" + mSerialNumber + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", hardwareVersion='" + hardwareVersion + '\'' +
                ", mRoutine=" + mRoutine +
                ", mListener=" + mListener +
                ", mShouldReconnect=" + mShouldReconnect +
                ", advertisedRssi=" + advertisedRssi +
                ", visibilityStatus=" + visibilityStatus +
                '}';
    }
}
