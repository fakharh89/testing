package io.blustream.sulley.routines.transactions.subtransactions;

import com.idevicesinc.sweetblue.BleConnectionPriority;
import com.idevicesinc.sweetblue.BleDevice;

import io.blustream.logger.Log;

public class ConnectionPrioritySubtransaction extends AbstractSubtransaction {
    private final BleConnectionPriority mPriority;
    private boolean mContinueOnFailure = true;

    public ConnectionPrioritySubtransaction(BleConnectionPriority priority) {
        mPriority = priority;
    }

    public boolean getContinueOnFailure() {
        return mContinueOnFailure;
    }

    public void setContinueOnFailure(boolean continueOnFailure) {
        mContinueOnFailure = continueOnFailure;
    }

    @Override
    protected boolean startFirstAction() {
        BleDevice.ReadWriteListener.ReadWriteEvent event
                = getDevice().setConnectionPriority(mPriority, readWriteEvent -> {
                    if (readWriteEvent.wasSuccess()) {
                        Log.i(getDeviceName() + " Set connection priority to " + mPriority);
                        succeed();
                    }
                    else {
                        if (mContinueOnFailure) {
                            Log.i(getDeviceName() + " Set connection priority to " + mPriority);
                            succeed();
                        }
                        else {
                            Log.w(getDeviceName() + " Failed to set connection priority to " + mPriority + "!");
                            fail();
                        }
                    }
                });

        if (!event.isNull()) {
            Log.w(getDeviceName() + " Failed to set connection priority to " + mPriority + "!");
            if (!mContinueOnFailure) {
                return false;
            }
        }

        return true;
    }
}
