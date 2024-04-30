package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;

import java.util.UUID;

import io.blustream.logger.Log;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

/**
 * Created by Ruzhitskii Sviatoslav on 9/20/19.
 */
public class KeyBlockDataTransferSubTransaction extends AbstractSubtransaction {

    private final BleDefinitions mDefinitions;
    private final byte[] toWrite;
    private boolean shouldtryAgain = true;

    public KeyBlockDataTransferSubTransaction(BleDefinitions mDefinitions, byte[] toWrite) {   // todo: use Utils_Byte;
        this.mDefinitions = mDefinitions;
        this.toWrite = toWrite;
    }

    @Override
    protected boolean startFirstAction() {
        listenForNotification();
        writeCharacteristic();
        return true;
    }

    private void writeCharacteristic() {
        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(getDeviceName() + " Wrote success");

            } else {
                Log.e(getDeviceName() + " Failed to write!");
                fail();
            }
        };

        getDevice().write(mDefinitions.getWriteBufferService(),
                mDefinitions.getWriteBufferCharacteristic(), toWrite, listener);
    }

    private void listenForNotification() {
        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                succeed();
            } else {
                Log.e(getDeviceName() + " Failed to read!");
                if (shouldtryAgain) {
                    listenForNotification();
                    shouldtryAgain = false;
                } else {
                    fail();
                }
            }
        };
        getDevice().read(mDefinitions.getReadBufferService(),
                mDefinitions.getReadBufferCharacteristic(), listener);
    }

    interface BleDefinitions {
        @NonNull
        @BleService
        UUID getReadBufferService();

        @NonNull
        @BleCharacteristic
        UUID getReadBufferCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getWriteBufferService();

        @NonNull
        @BleCharacteristic
        UUID getWriteBufferCharacteristic();
    }
}
