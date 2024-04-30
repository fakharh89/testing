package io.blustream.sulley.routines.transactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;
import com.idevicesinc.sweetblue.utils.Utils_Byte;

import java.util.UUID;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.BlinkMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public class BlinkTransaction extends BleTransaction {
    private final int mCount;
    private BleDefinitions mDefinitions;

    // TODO figure out how to use mappers to verify if the data is valid or not
    public BlinkTransaction(@NonNull BleDefinitions definitions, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count cannot be less than 0");
        }
        mCount = count;
        mDefinitions = definitions;
    }

    @Override
    protected void start(BleDevice device) {
        BlinkMapper mapper = new BlinkMapper();

        byte[] bytes;
        try {
            bytes = mapper.toBytes(mCount);
            Utils_Byte.reverseBytes(bytes);
        } catch (MapperException e) {
            fail();
            return;
        }

        Log.i(getDevice().getName_override() + " Writing blink command " + mCount);

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(getDevice().getName_override() + " Wrote blink command");
                succeed();
            } else {
                Log.e(getDevice().getName_override() + " Failed to write blink command");
                fail();
            }
        };

        getDevice().write(mDefinitions.getBlinkService(), mDefinitions.getBlinkCharacteristic(),
                bytes, listener);
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
        UUID getBlinkService();

        @NonNull
        @BleCharacteristic
        UUID getBlinkCharacteristic();
    }
}
