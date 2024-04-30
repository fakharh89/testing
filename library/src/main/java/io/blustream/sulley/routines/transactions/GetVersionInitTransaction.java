package io.blustream.sulley.routines.transactions;

import android.bluetooth.BluetoothGattService;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;
import com.idevicesinc.sweetblue.utils.Uuids;

import io.blustream.logger.Log;

public class GetVersionInitTransaction extends BleTransaction.Init {
    private String softwareVersion = null;
    private String hardwareVersion = null;
    private boolean isBootMode = false;
    private boolean mLog = false;

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public boolean isBootMode() {
        return isBootMode;
    }

    @Override
    protected void start(BleDevice device) {
        Log.i(getDevice().getName_override() + " Determining version");
        isBootMode = checkIfBootMode(device);
        if (isBootMode) {
            succeed();
            return;
        }

        readVersions();
    }

    private boolean checkIfBootMode(BleDevice device) {
        if (device.getNativeGatt().getService(io.blustream.sulley.utilities.Uuids.Otau.Boot.BOOT_OTAU_SERVICE) == null) {
            return false;
        }

        return true;
    }

    private void readVersions() {
        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                softwareVersion = readWriteEvent.data_string();
                Log.i(getDevice().getName_override() + " Version is " + softwareVersion);
                readHardwareVersion();
            } else {
                Log.e(getDevice().getName_override() + " Failed to get softwareVersion!");
                fail();
            }
        };

        getDevice().read(Uuids.DEVICE_INFORMATION_SERVICE_UUID, Uuids.SOFTWARE_REVISION,
                listener);
    }

    private void readHardwareVersion() {
        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                hardwareVersion = readWriteEvent.data_string();
                Log.i(getDevice().getName_override() + " HardwareVersion is " + hardwareVersion);
                succeed();
            } else {
                Log.e(getDevice().getName_override() + " Failed to get hardwareVersion!");
                fail();
            }
        };

        getDevice().read(Uuids.DEVICE_INFORMATION_SERVICE_UUID, Uuids.HARDWARE_REVISION,
                listener);
    }

    @Override
    protected void update(double v) {
        super.update(v);
        if (getTime() > 100) {
            Log.e(getDevice().getName_override() + " Timeout!");
            cancel();
        }
    }
}
