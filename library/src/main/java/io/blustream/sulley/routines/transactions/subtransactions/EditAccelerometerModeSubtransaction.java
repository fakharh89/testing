package io.blustream.sulley.routines.transactions.subtransactions;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.utils.Utils_Byte;

import java.util.UUID;

import androidx.annotation.NonNull;
import io.blustream.logger.Log;
import io.blustream.sulley.mappers.AccelerometerModeMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.AccelerometerMode;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public class EditAccelerometerModeSubtransaction extends AbstractSubtransaction {
    private final AccelerometerMode mMode;
    private final BleDefinitions mDefinitions;

    public EditAccelerometerModeSubtransaction(@NonNull BleDefinitions definitions, AccelerometerMode mode) {
        mDefinitions = definitions;
        if (mode == AccelerometerMode.UNKNOWN) {
            throw new IllegalArgumentException("Invalid mode to write!");
        }
        mMode = mode;
    }

    public interface BleDefinitions {
        @NonNull
        @BleService
        UUID getAccelerometerModeService();

        @NonNull
        @BleCharacteristic
        UUID getAccelerometerModeCharacteristic();
    }

    public AccelerometerMode getMode() {
        return mMode;
    }

    @Override
    protected boolean startFirstAction() {
        readMode();
        return true;
    }

    private void readMode() { // TODO fix naming
        Log.i(getDeviceName() + " Reading accelerometer mode");

        BleDevice.ReadWriteListener listener = e -> {
            if (e.wasSuccess()) {
                AccelerometerModeMapper mapper = new AccelerometerModeMapper();
                AccelerometerMode sensorMode;

                try {
                    sensorMode = mapper.fromBytes(e.data());
                } catch (MapperException exc) {
                    fail();
                    return;
                }

                Log.i(getDeviceName() + " Read accelerometer mode " + sensorMode);

                if (sensorMode == AccelerometerMode.UNKNOWN) {
                    fail();
                } else if (mMode == sensorMode) {
                    succeed();
                } else {
                    writeMode();
                }
            } else {
                Log.e(getDeviceName() + " Failed to read accelerometer mode!");
                fail();
            }
        };

        getDevice().read(mDefinitions.getAccelerometerModeService(),
                mDefinitions.getAccelerometerModeCharacteristic(), listener);
    }

    private void writeMode() {
        AccelerometerModeMapper mapper = new AccelerometerModeMapper();

        byte[] bytes;

        try {
            bytes = mapper.toBytes(mMode);
            Utils_Byte.reverseBytes(bytes);
        } catch (MapperException e) {
            fail();
            return;
        }

        Log.i(getDeviceName() + " Writing accelerometer mode " + mMode);

        BleDevice.ReadWriteListener listener = e -> {
            if (e.wasSuccess()) {
                Log.i(getDeviceName() + " Wrote accelerometer mode");
                succeed();
            }
            else {
                Log.e(getDeviceName() + " Failed to write accelerometer mode!");
                fail();
            }
        };

        getDevice().write(mDefinitions.getAccelerometerModeService(),
                mDefinitions.getAccelerometerModeCharacteristic(), bytes, listener);
    }
}
