package io.blustream.sulley.routines.transactions.subtransactions;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.utils.Utils_Byte;

import java.util.UUID;

import androidx.annotation.NonNull;
import io.blustream.logger.Log;
import io.blustream.sulley.mappers.HumidTempSamplingIntervalMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public class EditHumidTempSamplingIntervalSubtransaction extends AbstractSubtransaction {
    private final int mInterval;
    private final BleDefinitions mDefinitions;

    public EditHumidTempSamplingIntervalSubtransaction(@NonNull BleDefinitions definitions, int interval) {
        mDefinitions = definitions;
        if (interval <= 0) {
            throw new IllegalArgumentException("No negative values for intervals!");
        }
        mInterval = interval;
    }

    public interface BleDefinitions {
        @NonNull
        @BleService
        UUID getHumidTempSamplingIntervalService();

        @NonNull
        @BleCharacteristic
        UUID getHumidTempSamplingIntervalCharacteristic();
    }

    public int getInterval() {
        return mInterval;
    }

    @Override
    protected boolean startFirstAction() {
        readInterval();
        return true;
    }

    private void readInterval() {
        Log.i(getDeviceName() + " Reading humid temp sampling interval");

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                HumidTempSamplingIntervalMapper mapper = new HumidTempSamplingIntervalMapper();
                Integer sensorInterval;

                try {
                    sensorInterval = mapper.fromBytes(readWriteEvent.data());
                } catch (MapperException e) {
                    fail();
                    return;
                }

                Log.i(getDeviceName() + " Read humid temp sampling interval " + sensorInterval);

                if (mInterval == sensorInterval) {
                    succeed();
                }
                else {
                    writeInterval();
                }
            }
            else {
                Log.e(getDeviceName() + " Failed to read humid temp sampling interval!");
                fail();
            }
        };

        getDevice().read(mDefinitions.getHumidTempSamplingIntervalService(),
                mDefinitions.getHumidTempSamplingIntervalCharacteristic(), listener);
    }

    private void writeInterval() {
        Log.i(getDeviceName() + " Writing humid temp sampling interval " + mInterval);

        byte[] bytes = Utils_Byte.intToBytes(mInterval);
        Utils_Byte.reverseBytes(bytes);

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(getDeviceName() + " Wrote humid temp sampling interval");
                succeed();
            }
            else {
                Log.e(getDeviceName() + " Failed to write humid temp sampling interval!");
                fail();
            }
        };

        getDevice().write(mDefinitions.getHumidTempSamplingIntervalService(),
                mDefinitions.getHumidTempSamplingIntervalCharacteristic(), bytes, listener);
    }
}
