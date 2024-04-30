package io.blustream.sulley.routines.transactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;

import java.util.Random;

import io.blustream.logger.Log;

public class RegistrationTransaction2 extends BleTransaction {
    @NonNull
    private final RegistrationTransaction.BleDefinitions mDefinitions;

    public RegistrationTransaction2(@NonNull RegistrationTransaction.BleDefinitions definitions) {
        mDefinitions = definitions;
    }

    @Override
    protected void start(BleDevice device) {
        getDevice().enableNotify(mDefinitions.getRegistrationService(),
                mDefinitions.getRegistrationCharacteristic(), e -> {
                    if (!e.wasSuccess()) {
                        Log.e(getDevice().getName_override() + " Failed to enable reg notify!");
                        fail();
                        return;
                    }

                    if (e.type() == BleDevice.ReadWriteListener.Type.ENABLING_NOTIFICATION) {
                        Log.i(getDevice().getName_override() + " Enabled reg notify.");
                        writeCharacteristic();
                        return;
                    }

                    succeed();
                });
    }

    private void writeCharacteristic() {
        byte[] bytes = new byte[16];
        Random random = new Random(System.currentTimeMillis());
        random.nextBytes(bytes);

        getDevice().write(mDefinitions.getRegistrationService(),
                mDefinitions.getRegistrationCharacteristic(), bytes, e -> {
                    if (!e.wasSuccess()) {
                        Log.e(getDevice().getName_override() + " Failed to write reg data!");
                        fail();
                        return;
                    }

                    Log.i(getDevice().getName_override() + " Write reg data. Waiting for notify.");
                });
    }
}
