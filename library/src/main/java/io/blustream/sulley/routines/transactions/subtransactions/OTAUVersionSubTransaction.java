package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;

import io.blustream.logger.Log;
import io.blustream.sulley.routines.OTAUBleDefinitions;
import io.blustream.sulley.routines.OTAUBootModeBleDefinitions;

public class OTAUVersionSubTransaction extends BleTransaction {
    private Integer otauVersion = null;
    private boolean mLog = false;
    private OTAUBleDefinitions mDefinitions;

    public Integer getOtauVersion() {
        return otauVersion;
    }

    public OTAUVersionSubTransaction(@NonNull OTAUBleDefinitions definitions) {
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

        getDevice().read(mDefinitions.getApplicationService(), mDefinitions.getOTAUVersionCharacteristic(),
                listener);
    }

}
