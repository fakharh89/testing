package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;


import java.util.UUID;

import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;


import io.blustream.logger.Log;
import io.blustream.sulley.routines.OTAUBootModeBleDefinitions;

public class BootModeOTAUVersionSubTransaction extends BleTransaction {
    private Integer otauVersion = null;
    private boolean mLog = false;
    private OTAUBootModeBleDefinitions mDefinitions;

    public Integer getOtauVersion() {
        return otauVersion;
    }

    public BootModeOTAUVersionSubTransaction(@NonNull OTAUBootModeBleDefinitions definitions) {
        mDefinitions = definitions;
    }

    protected void start(BleDevice device) {
        Log.i(getDevice().getName_override() + " Determining OTAU version");
        readVersion();
    }

    private void readVersion() {
        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                otauVersion = readWriteEvent.data_int(false);
                Log.i(getDevice().getName_override() + " OTAU Version is " + otauVersion);
                succeed();
            } else {
                Log.e(getDevice().getName_override() + " Failed to get OTAU Version!");
                fail();
            }
        };

        getDevice().read(mDefinitions.getBootModeOTAUVersionService(), mDefinitions.getBootModeOTAUVersionCharacteristic(),
                listener);
    }

}
