package io.blustream.sulley.routines.transactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleConnectionPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.blustream.logger.Log;
import io.blustream.sulley.models.AccelerometerMode;
import io.blustream.sulley.routines.transactions.subtransactions.ConnectionPrioritySubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.EditAccelerometerModeSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.EditHumidTempSamplingIntervalSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.EditImpactThresholdSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.Subtransaction;

public class CheckEditableSettingsTransaction extends QueuedTransaction {
    public CheckEditableSettingsTransaction(@NonNull BleDefinitions definitions, Settings settings) {
        List<Subtransaction> subtransactions = new ArrayList<>();

        if (settings.getAccelerometerMode() != null
                && settings.getAccelerometerMode() != AccelerometerMode.UNKNOWN) {
            subtransactions.add(new EditAccelerometerModeSubtransaction(definitions, settings.getAccelerometerMode()));
        }

        if (settings.getHumidTempSamplingInterval() != null) {
            subtransactions.add(new EditHumidTempSamplingIntervalSubtransaction(definitions, settings.getHumidTempSamplingInterval()));
        }

        if (settings.getImpactThreshold() != null
                && settings.getImpactThresholdTolerance() != null) {
            subtransactions.add(new EditImpactThresholdSubtransaction(definitions, settings.getImpactThreshold(),
                    settings.getImpactThresholdTolerance()));
        }

        if (subtransactions.size() > 0) {
            ConnectionPrioritySubtransaction highPrioritySubtransaction
                    = new ConnectionPrioritySubtransaction(BleConnectionPriority.HIGH);
            // Add to start of list
            subtransactions.add(0, highPrioritySubtransaction);

            ConnectionPrioritySubtransaction mediumPrioritySubtransaction
                    = new ConnectionPrioritySubtransaction(BleConnectionPriority.MEDIUM);
            subtransactions.add(mediumPrioritySubtransaction);
        }

        setSubtransactionQueue(new ConcurrentLinkedQueue<>(subtransactions));
    }

    @Override
    protected void update(double v) {
        super.update(v);
        if (getTime() > 100) {
            Log.e(getDevice().getName_override() + " Timeout!");
            cancel();
        }
    }

    @Override
    protected boolean needsAtomicity() {
        return true;
    }

    public interface BleDefinitions extends EditAccelerometerModeSubtransaction.BleDefinitions,
            EditHumidTempSamplingIntervalSubtransaction.BleDefinitions,
            EditImpactThresholdSubtransaction.BleDefinitions {
    }

    public interface Settings {
        Integer getHumidTempSamplingInterval();

        AccelerometerMode getAccelerometerMode();

        Float getImpactThreshold();

        Float getImpactThresholdTolerance();
    }
}
