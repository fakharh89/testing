package io.blustream.sulley.routines.transactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;

import java.util.UUID;

import io.blustream.logger.Log;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public class RealtimeTransaction extends BleTransaction {
    private BleDefinitions mDefinitions;

    public RealtimeTransaction(@NonNull BleDefinitions definitions) {
        mDefinitions = definitions;
    }

    @Override
    protected void start(BleDevice device) {
        byte[] data = new byte[1];
        data[0] = 0;

        Log.i(getDevice().getName_override() + " Writing realtime data command");

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(getDevice().getName_override() + " Wrote realtime data command");
                succeed();
            } else {
                Log.e(getDevice().getName_override() + " Failed to write realtime data command!");
                fail();
            }
        };

        getDevice().write(mDefinitions.getRealtimeService(),
                mDefinitions.getRealtimeHumidTempCharacteristic(), data, listener);
    }

    @Override
    protected void update(double v) {
        super.update(v);
        if (getTime() > 100) {
            Log.e(getDevice().getName_override() + " Timeout!");
            cancel();
        }
    }

    public interface BleDefinitions {
        @NonNull
        @BleService
        UUID getRealtimeService();

        @NonNull
        @BleCharacteristic
        UUID getRealtimeHumidTempCharacteristic();
    }
}
