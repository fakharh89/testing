package io.blustream.sulley.routines.transactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;
import com.idevicesinc.sweetblue.utils.Utils_Byte;

import java.util.Random;
import java.util.UUID;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.RegistrationMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;
import io.blustream.sulley.utilities.RetryHelper;

public class RegistrationTransaction extends BleTransaction {

    private static final int MAX_RETRY_ATTEMPT = 2;
    private static final int RETRY_ATTEMPT_PERIOD_MILLIS = 10_000;

    @NonNull
    private final BleDefinitions mDefinitions;
    private Random random = new Random(System.currentTimeMillis());
    private RegistrationMapper mapper = new RegistrationMapper();
    private RetryHelper retryHelper = new RetryHelper(MAX_RETRY_ATTEMPT, RETRY_ATTEMPT_PERIOD_MILLIS);
    private BleDevice.ReadWriteListener listener = readWriteEvent -> {
        retryHelper.stop();
        if (readWriteEvent.wasSuccess()) {
            Log.i(getDevice().getName_override() + " Registration succeed.");
            succeed();
        } else {
            Log.i(getDevice().getName_override() + " Registration failed.");
            fail();
        }
    };
    private RetryHelper.RetryListener retryListener = new RetryHelper.RetryListener() {
        @Override
        public void onRetry() {
            writeCharacteristic();
        }

        @Override
        public void onTimeOut() {
            fail();
        }
    };

    public RegistrationTransaction(@NonNull BleDefinitions definitions) {
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
                        retryHelper.start(retryListener);
                        writeCharacteristic();
                        return;
                    }

//                    succeed();
                });
//        writeCharacteristic();
    }

    private void writeCharacteristic() {
        byte[] bytes;
        try {
            bytes = mapper.toBytes(random.nextInt());
        } catch (MapperException e) {
            Log.e(getDevice().getName_override() + " Failed to map registration data! " + e);
            retryHelper.stop();
            fail();
            return;
        }
        Utils_Byte.reverseBytes(bytes);

        getDevice().write(mDefinitions.getRegistrationService(), mDefinitions.getRegistrationCharacteristic(), bytes, listener);
    }

    public interface BleDefinitions {
        @NonNull
        @BleService
        UUID getRegistrationService();

        @NonNull
        @BleCharacteristic
        UUID getRegistrationCharacteristic();
    }
}
