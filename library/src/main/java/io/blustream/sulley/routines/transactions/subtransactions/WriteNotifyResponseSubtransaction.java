package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.utils.Interval;

import java.util.UUID;

import io.blustream.logger.Log;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public abstract class WriteNotifyResponseSubtransaction extends AbstractSubtransaction {
    private final BleDefinitions mDefinitions;
    private byte[] mResponseValue;

    public WriteNotifyResponseSubtransaction(@NonNull BleDefinitions definitions) {
        mDefinitions = definitions;
    }

    public interface BleDefinitions {
        @NonNull
        @BleService
        UUID getService();

        @NonNull
        @BleCharacteristic
        UUID getWriteCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getNotifyResponseCharacteristic();
    }

    public byte[] getResponseValue() {
        return mResponseValue;
    }

    public abstract byte[] getWriteValue();

    @Override
    protected boolean startFirstAction() {
        if (getWriteValue() == null || getWriteValue().length == 0) {
            Log.e(getDeviceName() + " Write value is null or empty!");
            fail();
            return false;
        }

        Interval interval = Interval.ONE_SEC;

        BleDevice.ReadWriteListener readWriteListener = new BleDevice.ReadWriteListener() {
            @Override
            public void onEvent(ReadWriteEvent e) {
                if (e.wasSuccess()) {
                    Log.i(getDeviceName() + " Read notify value!");
                    getDevice().stopPoll(mDefinitions. getService(),
                            mDefinitions.getNotifyResponseCharacteristic(),
                            interval, this);
                    mResponseValue = e.data();
                    succeed();
                } else {
                    Log.e(getDeviceName() + "Failed to read notify value!");
                    getDevice().stopPoll(mDefinitions. getService(),
                            mDefinitions.getNotifyResponseCharacteristic(),
                            interval, this);
                    mResponseValue = e.data();
                    fail();
                }
            }
        };

        getDevice().startPoll(mDefinitions.getService(),
                mDefinitions.getNotifyResponseCharacteristic(), interval, readWriteListener);

        getDevice().write(mDefinitions.getService(), mDefinitions.getWriteCharacteristic(),
                getWriteValue(), new BleDevice.ReadWriteListener() {
            @Override
            public void onEvent(ReadWriteEvent e) {
                if (e.wasSuccess()) {
                    Log.e(getDeviceName() + " Wrote value for notify response!");
                }
                else {
                    Log.e(getDeviceName() + " Failed to write value for notify response!");
                    fail();
                    getDevice().stopPoll(mDefinitions.getService(),
                            mDefinitions.getNotifyResponseCharacteristic(), interval,
                            readWriteListener);
                }
            }
        });

        return true;
    }
}
