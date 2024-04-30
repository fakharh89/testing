package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;
import com.idevicesinc.sweetblue.utils.FutureData;

import java.util.UUID;

import io.blustream.logger.Log;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public class BeaconModeTransaction extends BleTransaction {

    private BleDefinitions mDefinitions;
    private int mMode;

    public BeaconModeTransaction(@NonNull BleDefinitions definitions, int mode) {
        mDefinitions = definitions;
        mMode = mode;
    }

    @Override
    protected void start(BleDevice device) {
        Log.i(getDevice().getName_override() + " Writing Beacon Mode");
        FutureData futureData = () -> new byte[mMode];
        BleDevice.ReadWriteListener listener = e -> {
            if (e.wasSuccess()) {
                Log.i(getDevice().getName_override() + " Wrote Beacon Mode");
                succeed();
            } else {
                Log.e(getDevice().getName_override() + " Failed to write Beacon Mode");
                fail();
            }
        };

        getDevice().write(mDefinitions.getBeaconModeService(), mDefinitions.getBeaconModeCharacteristic(), futureData, listener);
    }

    public interface BleDefinitions {
        @NonNull
        @BleService
        UUID getBeaconModeService();

        @NonNull
        @BleCharacteristic
        UUID getBeaconModeCharacteristic();
    }
}
