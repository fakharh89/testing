package io.blustream.sulley.routines.transactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;
import com.idevicesinc.sweetblue.utils.FutureData;

import java.util.Date;
import java.util.UUID;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.DateMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public class SyncTimeTransaction extends BleTransaction {
    private BleDefinitions mDefinitions;

    public SyncTimeTransaction(@NonNull BleDefinitions definitions) {
        mDefinitions = definitions;
    }

    @Override
    protected void start(BleDevice device) {
        Log.i(getDevice().getName_override() + " Writing sensor time");

        FutureData futureData = () -> {
            DateMapper mapper = new DateMapper();
            try {
                return mapper.toBytes(new Date());
            } catch (MapperException e) {
                // Returning an empty array will throw a failed event in the ReadWriteListener.
                // Returning null will cause SweetBlue to crash.  Don't do that.
                Log.e(getDevice().getName_override() + " Couldn't map date object to sync time!");
                return new byte[0];
            }
        };

        BleDevice.ReadWriteListener listener = e -> {
            if (e.wasSuccess()) {
                Log.i(getDevice().getName_override() + " Wrote sensor time");
                succeed();
            } else {
                Log.e(getDevice().getName_override() + " Failed to write sensor time");
                fail();
            }
        };

        getDevice().write(mDefinitions.getTimeSyncService(), mDefinitions.getTimeSyncCharacteristic(),
                futureData, listener);
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
        UUID getTimeSyncService();

        @NonNull
        @BleCharacteristic
        UUID getTimeSyncCharacteristic();
    }
}
