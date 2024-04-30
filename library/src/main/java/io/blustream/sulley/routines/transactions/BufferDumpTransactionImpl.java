package io.blustream.sulley.routines.transactions;

import com.idevicesinc.sweetblue.BleConnectionPriority;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.HumidTempBufferMapper;
import io.blustream.sulley.mappers.ImpactBufferMapper;
import io.blustream.sulley.mappers.MotionBufferMapper;
import io.blustream.sulley.models.HumidTempSample;
import io.blustream.sulley.models.ImpactSample;
import io.blustream.sulley.models.MotionSample;
import io.blustream.sulley.routines.transactions.subtransactions.BufferSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.BufferSubtransactionBleDefinitionsImpl;
import io.blustream.sulley.routines.transactions.subtransactions.BufferSubtransactionImpl;
import io.blustream.sulley.routines.transactions.subtransactions.ConnectionPrioritySubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.Subtransaction;

public class BufferDumpTransactionImpl extends QueuedTransaction {

    public BufferDumpTransactionImpl(BufferDumpTransaction.BleDefinitions definitions,
                                     boolean succeedOnDisconnectAfterDelete,
                                     BufferDumpTransaction.Listener listener) {
        ConcurrentLinkedQueue<Subtransaction> subtransactions = new ConcurrentLinkedQueue<>();

        ConnectionPrioritySubtransaction highPrioritySubtransaction
                = new ConnectionPrioritySubtransaction(BleConnectionPriority.HIGH);
        subtransactions.add(highPrioritySubtransaction);

        BufferSubtransactionBleDefinitionsImpl humidTempDefinitions
                = new BufferSubtransactionBleDefinitionsImpl(definitions.getBufferService(),
                definitions.getHumidTempBufferCharacteristic(),
                definitions.getHumidTempBufferSizeCharacteristic());

        BufferSubtransactionImpl<HumidTempSample> humidTempSubtransaction
                = new BufferSubtransactionImpl<>(humidTempDefinitions, new HumidTempBufferMapper(),
                succeedOnDisconnectAfterDelete);

        humidTempSubtransaction.setListener(new BufferSubtransaction.Listener<HumidTempSample>() {
            @Override
            public void didGetData(List<HumidTempSample> samples) {
                if (listener != null) {
                    listener.didGetHumidTempSamples(samples);
                }
            }

            @Override
            public void clearedBuffer() {
                if (listener != null) {
                    listener.didClearHumidTempBuffer();
                }
            }

            @Override
            public void didEncounterError() {

            }
        });

        subtransactions.add(humidTempSubtransaction);

        BufferSubtransactionBleDefinitionsImpl impactDefinitions
                = new BufferSubtransactionBleDefinitionsImpl(definitions.getBufferService(),
                definitions.getImpactBufferCharacteristic(),
                definitions.getImpactBufferSizeCharacteristic());

        BufferSubtransactionImpl<ImpactSample> impactSubtransaction
                = new BufferSubtransactionImpl<>(impactDefinitions, new ImpactBufferMapper(),
                succeedOnDisconnectAfterDelete);

        impactSubtransaction.setListener(new BufferSubtransaction.Listener<ImpactSample>() {
            @Override
            public void didGetData(List<ImpactSample> samples) {
                if (listener != null) {
                    listener.didGetImpactSamples(samples);
                }
            }

            @Override
            public void clearedBuffer() {
                if (listener != null) {
                    listener.didClearImpactBuffer();
                }
            }

            @Override
            public void didEncounterError() {

            }
        });

        subtransactions.add(impactSubtransaction);

        BufferSubtransactionBleDefinitionsImpl motionDefinitions
                = new BufferSubtransactionBleDefinitionsImpl(definitions.getBufferService(),
                definitions.getMotionBufferCharacteristic(),
                definitions.getMotionBufferSizeCharacteristic());

        BufferSubtransactionImpl<MotionSample> motionSubtransaction
                = new BufferSubtransactionImpl<>(motionDefinitions, new MotionBufferMapper(),
                succeedOnDisconnectAfterDelete);

        motionSubtransaction.setListener(new BufferSubtransaction.Listener<MotionSample>() {
            @Override
            public void didGetData(List<MotionSample> samples) {
                if (listener != null) {
                    listener.didGetMotionSamples(samples);
                }
            }

            @Override
            public void clearedBuffer() {
                if (listener != null) {
                    listener.didClearMotionBuffer();
                }
            }

            @Override
            public void didEncounterError() {

            }
        });

        subtransactions.add(motionSubtransaction);

        ConnectionPrioritySubtransaction mediumPrioritySubtransaction
                = new ConnectionPrioritySubtransaction(BleConnectionPriority.MEDIUM);
        subtransactions.add(mediumPrioritySubtransaction);

        setSubtransactionQueue(subtransactions);
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
}
