package io.blustream.sulley.routines.transactions.subtransactions;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.utils.Utils_Byte;

import java.util.UUID;

import androidx.annotation.NonNull;
import io.blustream.logger.Log;
import io.blustream.sulley.mappers.ImpactThresholdMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;

public class EditImpactThresholdSubtransaction extends AbstractSubtransaction {
    private final float mThreshold;
    private final float mTolerance;
    private final BleDefinitions mDefinitions;

    public EditImpactThresholdSubtransaction(@NonNull BleDefinitions definitions, float threshold, float tolerance) {

        mDefinitions = definitions;
        if (threshold < 0) {
            throw new IllegalArgumentException("No negative values for threshold!");
        }
        if (tolerance < 0) {
            throw new IllegalArgumentException("No negative values for tolerance!");
        }
        mThreshold = threshold;
        mTolerance = tolerance;
    }

    public interface BleDefinitions {
        @NonNull
        @BleService
        UUID getImpactThresholdService();

        @NonNull
        @BleCharacteristic
        UUID getImpactThresholdCharacteristic();
    }

    public float getThreshold() {
        return mThreshold;
    }

    @Override
    protected boolean startFirstAction() {
        readThreshold();
        return true;
    }

    private void readThreshold() {
        Log.i(getDeviceName() + " Reading impact threshold");

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                ImpactThresholdMapper mapper = new ImpactThresholdMapper();
                Float sensorThreshold;

                try {
                    sensorThreshold = mapper.fromBytes(readWriteEvent.data());
                } catch (MapperException e) {
                    fail();
                    return;
                }

                Log.i(getDeviceName() + " Read impact threshold " + sensorThreshold);

                if (thresholdsAreWithinTolerance(mThreshold, sensorThreshold, mTolerance)) {
                    succeed();
                }
                else {
                    writeInterval();
                }
            }
            else {
                Log.e(getDeviceName() + " Failed to read impact threshold!");
                fail();
            }
        };

        getDevice().read(mDefinitions.getImpactThresholdService(),
                mDefinitions.getImpactThresholdCharacteristic(), listener);
    }

    // TODO Validate that this works correctly
    // Probably need to write a function that rounds to the nearest 0.0625 interval,
    // then calculates the difference
    private boolean thresholdsAreWithinTolerance(@NonNull Float threshold1, @NonNull Float threshold2, @NonNull Float tolerance) {
        float difference = Math.abs(threshold1 - threshold2);

        return difference <= tolerance;
    }

    private void writeInterval() {
        // TODO use mapper
        short sensorThreshold = (short)Math.round(mThreshold / 0.0625f);

        Log.i(getDeviceName() + " Writing impact threshold " + mThreshold + " " + sensorThreshold);

        byte[] bytes = Utils_Byte.shortToBytes(sensorThreshold);
        Utils_Byte.reverseBytes(bytes);

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(getDeviceName() + " Wrote impact threshold");
                succeed();
            }
            else {
                Log.e(getDeviceName() + " Failed to write impact threshold!");
                fail();
            }
        };

        getDevice().write(mDefinitions.getImpactThresholdService(),
                mDefinitions.getImpactThresholdCharacteristic(), bytes, listener);
    }
}
