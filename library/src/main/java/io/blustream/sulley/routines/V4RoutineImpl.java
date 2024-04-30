package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;

import io.blustream.logger.Log;
import io.blustream.sulley.routines.transactions.subtransactions.BeaconModeTransaction;
import io.blustream.sulley.sensor.Sensor;

public class V4RoutineImpl extends BlustreamRoutineImpl {

    private static final int BEACON_MODE_ENABLED = 0x01;

    private RoutineConfig mConfig;

    public V4RoutineImpl(@NonNull Sensor sensor, @NonNull RoutineConfig config) {
        super(sensor, new V4BleDefinitions(), getOptions());
        mConfig = config;
    }

    private static Options getOptions() {
        return new Options() {
            @Override
            public boolean succeedOnDisconnectAfterDelete() {
                return true;
            }

            @Override
            public boolean editSettingsBeforeGettingData() {
                return true;
            }
        };
    }

    @Override
    protected void onSyncTimeSuccess() {
        startBeaconModeTransaction();
        super.onSyncTimeSuccess();
    }

    private void startBeaconModeTransaction() {
        if (mConfig.isBeaconModeEnabled()) {
            getSensor().getBleDevice().performTransaction(new BeaconModeTransaction((V4BleDefinitions) mDefinitions, BEACON_MODE_ENABLED) {
                @Override
                protected void onEnd(BleDevice device, EndReason endReason) {
                    super.onEnd(device, endReason);
                    if (endReason != EndReason.SUCCEEDED) {
                        Log.e(mSensor.getSerialNumber() + "Failed to write value to Beacon Mode characteristic");
                    }
                }
            });
        }
    }

    @Override
    public boolean isSensorCompatible(Sensor sensor) {
        boolean areCharsComp = DefinitionCheckHelper.checkSensor(mDefinitions, sensor);
        boolean isV4 = sensor.getSoftwareVersion().startsWith("4");
        return areCharsComp && isV4;
    }
}
