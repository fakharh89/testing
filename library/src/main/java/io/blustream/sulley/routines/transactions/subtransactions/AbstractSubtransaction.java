package io.blustream.sulley.routines.transactions.subtransactions;

import com.idevicesinc.sweetblue.BleDevice;

abstract class AbstractSubtransaction implements Subtransaction {
    private SubtransactionDelegate mDelegate;
    private boolean mRunning = false;

    public final boolean isRunning() {
        return mRunning;
    }

    @Override
    public boolean start() {
        if (mRunning) {
            return false;
        }

        mRunning = true;

        return startFirstAction();
    }

    protected abstract boolean startFirstAction();

    @Override
    public void onEnd(boolean success, boolean cancelPending) {
        mRunning = false;
        if (this.mDelegate != null) {
            mDelegate.ended(this, success, cancelPending);
        }
    }

    @Override
    public void succeed() {
        onEnd(true, false);
    }

    @Override
    public void succeed(boolean cancelPending) {
        onEnd(true, cancelPending);
    }

    @Override
    public void fail() {
        onEnd(false, false);
    }

    @Override
    public void fail(boolean cancelPending) {
        onEnd(false, cancelPending);
    }

    @Override
    public BleDevice getDevice() {
        if (this.mDelegate != null) {
            return mDelegate.getDevice(this);
        }
        else {
            return null;
        }
    }

    @Override
    public void setDelegate(SubtransactionDelegate delegate) {
        mDelegate = delegate;
    }

    protected String getDeviceName() {
        return getDevice().getName_override();
    }
}
