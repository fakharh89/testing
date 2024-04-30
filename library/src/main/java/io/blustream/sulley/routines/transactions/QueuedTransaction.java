package io.blustream.sulley.routines.transactions;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.blustream.logger.Log;
import io.blustream.sulley.routines.transactions.subtransactions.Subtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.SubtransactionDelegate;

public class QueuedTransaction extends BleTransaction.Ota implements SubtransactionDelegate {
    private ConcurrentLinkedQueue<Subtransaction> mSubtransactionQueue;

    public QueuedTransaction() {
        this(null);
    }

    public QueuedTransaction(ConcurrentLinkedQueue<Subtransaction> subtransactionQueue) {
        super();
        mSubtransactionQueue = subtransactionQueue;
    }

    public void setSubtransactionQueue(ConcurrentLinkedQueue<Subtransaction> subtransactionQueue) {
        mSubtransactionQueue = subtransactionQueue;
    }

    public ConcurrentLinkedQueue<Subtransaction> getSubtransactionQueue() {
        return mSubtransactionQueue;
    }

    @Override
    protected void start(BleDevice device) {
        Log.i(getDevice().getName_override() + " Starting queued transaction");
        startPeeking();
    }

    private void startPeeking() {
        if (mSubtransactionQueue == null) {
            Log.i(getDevice().getName_override() + " Subtransaction queue is null!");
            fail();
            return;
        }

        Subtransaction subtransaction = mSubtransactionQueue.peek();

        if (subtransaction == null) {
            Log.i(getDevice().getName_override() + " Finished running queued subtransactions");
            succeed();
            return;
        }

        subtransaction.setDelegate(this);

        if (!subtransaction.start()) {
            Log.e(getDevice().getName_override() + " Failed to start running subtransactions!");
            fail();
        } else {
            Log.i(getDevice().getName_override() + " Started queued subtransaction");
        }
    }

    @Override
    public void ended(Subtransaction subtransaction, boolean success, boolean cancelPending) {
        if (!mSubtransactionQueue.contains(subtransaction)) {
            Log.e(getDevice().getName_override() + " Internal subtransaction queue issue!");
            fail();
            return;
        }

        if (!success) {
            Log.e(getDevice().getName_override() + " Subtransaction failed!");
            fail();
            return;
        }

        Log.i(getDevice().getName_override() + " Queued subtransaction finished");

        if (cancelPending) {
            Log.i(getDevice().getName_override() + " Canceling pending subtransactions");
            mSubtransactionQueue.clear();
            succeed();
            return;
        }

        mSubtransactionQueue.remove(subtransaction);

        startPeeking();
    }

    @Override
    public BleDevice getDevice(Subtransaction subtransaction) {
        return getDevice();
    }

    @Override
    protected void update(double v) {
        super.update(v);
        if (getTime() > 100) {
            Log.e(getDevice().getName_override() + " Timeout!");
            cancel();
        }
    }
}
