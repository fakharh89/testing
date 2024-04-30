package io.blustream.sulley.sensor;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleNodeConfig;
import com.idevicesinc.sweetblue.utils.Interval;

import io.blustream.logger.Log;

class MyDefaultReconnectFilter implements BleNodeConfig.ReconnectFilter {
    private final Please mPleaseShortTermShouldTryAgain;
    private final Please mPleaseLongTermShouldTryAgain;

    private final Interval mTimeoutShortTermShouldContinue;
    private final Interval mTimeoutLongTermShouldContinue;

    public MyDefaultReconnectFilter() {
        mPleaseShortTermShouldTryAgain = Please.retryIn(Interval.secs(1.0));
        mPleaseLongTermShouldTryAgain = Please.retryIn(Interval.secs(3.0));
        mTimeoutShortTermShouldContinue = Interval.FIVE_SECS;
        mTimeoutLongTermShouldContinue = Interval.mins(5);
    }

    @Override
    public Please onEvent(final ReconnectEvent reconnectEvent) {
        if (reconnectEvent.type().isShouldTryAgain()) {
            if (reconnectEvent.failureCount() == 0) {
                return Please.retryInstantly();
            }
            else {
                if (reconnectEvent.type().isShortTerm()) {
                    return mPleaseShortTermShouldTryAgain;
                }
                else {
                    return mPleaseLongTermShouldTryAgain;
                }
            }
        }
        else if (reconnectEvent.type().isShouldContinue()) {
            if (reconnectEvent.node() instanceof BleDevice) {
                final boolean definitelyPersist = BleDeviceState.CONNECTING_OVERALL.overlaps(reconnectEvent.device().getNativeStateMask()) &&
                        BleDeviceState.CONNECTED.overlaps(reconnectEvent.device().getNativeStateMask());

                //--- DRK > We don't interrupt if we're in the middle of connecting
                //---		but this will be the last attempt if it fails.
                if (definitelyPersist) {
                    return Please.persist();
                }
                else {
                    return shouldContinue(reconnectEvent);
                }
            }
            else {
                return shouldContinue(reconnectEvent);
            }
        }
        else {
            //Log.w(reconnectEvent.device().getName_override() + " Reconnect event - Please stop retrying");
            return Please.stopRetrying();
        }
    }

    private Please shouldContinue(final ReconnectEvent reconnectEvent) {
        if (reconnectEvent.type().isShortTerm()) {
            return Please.persistIf(reconnectEvent.totalTimeReconnecting().lt(mTimeoutShortTermShouldContinue));
        }
        else {
//            return Please.persistIf(e.totalTimeReconnecting().lt(mTimeoutLongTermShouldContinue));
            return Please.persist();
        }
    }
}