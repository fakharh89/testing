package io.blustream.sulley.sensor;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleNodeConfig;
import com.idevicesinc.sweetblue.utils.Interval;

import java.util.Date;
import java.util.HashMap;

import io.blustream.logger.Log;

class SweetBlueDefaultReconnectFilter implements BleNodeConfig.ReconnectFilter
{
    public static final Please DEFAULT_INITIAL_RECONNECT_DELAY	= Please.retryInstantly();

    public static final Interval LONG_TERM_ATTEMPT_RATE			= Interval.secs(3.0);
    public static final Interval SHORT_TERM_ATTEMPT_RATE		= Interval.secs(1.0);

    public static final Interval SHORT_TERM_TIMEOUT				= Interval.FIVE_SECS;
    public static final Interval LONG_TERM_TIMEOUT				= Interval.mins(5);

    private final Please m_please__SHORT_TERM__SHOULD_TRY_AGAIN;
    private final Please m_please__LONG_TERM__SHOULD_TRY_AGAIN;

    private final Interval m_timeout__SHORT_TERM__SHOULD_CONTINUE;
    private final Interval m_timeout__LONG_TERM__SHOULD_CONTINUE;

    private HashMap<String, Date> lastLongTermLogDate = new HashMap<>();
    private HashMap<String, Date> lastShortTermLogDate = new HashMap<>();
    private HashMap<String, Date> lastPersistDate = new HashMap<>();

    public SweetBlueDefaultReconnectFilter()
    {
        this
                (
                        BleNodeConfig.DefaultReconnectFilter.SHORT_TERM_ATTEMPT_RATE,
                        BleNodeConfig.DefaultReconnectFilter.LONG_TERM_ATTEMPT_RATE,
                        BleNodeConfig.DefaultReconnectFilter.SHORT_TERM_TIMEOUT,
                        BleNodeConfig.DefaultReconnectFilter.LONG_TERM_TIMEOUT
                );
    }

    public SweetBlueDefaultReconnectFilter(final Interval reconnectRate__SHORT_TERM, final Interval reconnectRate__LONG_TERM, final Interval timeout__SHORT_TERM, final Interval timeout__LONG_TERM)
    {
        m_please__SHORT_TERM__SHOULD_TRY_AGAIN = Please.retryIn(reconnectRate__SHORT_TERM);
        m_please__LONG_TERM__SHOULD_TRY_AGAIN = Please.retryIn(reconnectRate__LONG_TERM);

        m_timeout__SHORT_TERM__SHOULD_CONTINUE = timeout__SHORT_TERM;
        m_timeout__LONG_TERM__SHOULD_CONTINUE = timeout__LONG_TERM;
    }

    @Override public Please onEvent(final ReconnectEvent e)
    {
        if( e.type().isShouldTryAgain() )
        {
            if( e.failureCount() == 0 )
            {
                Log.w(e.device().getName_override() + " ReconnectEvent: Please.retryInstantly() - first failure");
                return DEFAULT_INITIAL_RECONNECT_DELAY;
            }
            else
            {
                if( e.type().isShortTerm() )
                {
                    Log.w(e.device().getName_override() + " ReconnectEvent: Please.retryIn(reconnectRate__SHORT_TERM)");
                    return m_please__SHORT_TERM__SHOULD_TRY_AGAIN;
                }
                else
                {
                    Log.w(e.device().getName_override() + " ReconnectEvent: Please.retryIn(reconnectRate__LONG_TERM)");
                    return m_please__LONG_TERM__SHOULD_TRY_AGAIN;
                }
            }
        }
        else if( e.type().isShouldContinue() )
        {
            if( e.node() instanceof BleDevice)
            {
                final boolean definitelyPersist = BleDeviceState.CONNECTING_OVERALL.overlaps(e.device().getNativeStateMask()) &&
                        BleDeviceState.CONNECTED.overlaps(e.device().getNativeStateMask());

                //--- DRK > We don't interrupt if we're in the middle of connecting
                //---		but this will be the last attempt if it fails.
                if( definitelyPersist )
                {
                    if (shouldLogMessage(lastPersistDate, e.device().getName_override())) {
                        lastPersistDate.put(e.device().getName_override(), new Date());
                        Log.w(e.device().getName_override() + " ReconnectEvent: Please.persist()");
                    }
                    return Please.persist();
                }
                else
                {
                    return shouldContinue(e);
                }
            }
            else
            {
                return shouldContinue(e);
            }
        }
        else
        {
            Log.e(e.device().getName_override() + " ReconnectEvent: Please.stopRetrying() - shouldn't try again or continue");
            return Please.stopRetrying();
        }
    }

    private Please shouldContinue(final ReconnectEvent e)
    {
        if( e.type().isShortTerm() )
        {
            if (shouldLogMessage(lastShortTermLogDate, e.device().getName_override())) {
                lastShortTermLogDate.put(e.device().getName_override(), new Date());
                boolean r = e.totalTimeReconnecting().lt(m_timeout__SHORT_TERM__SHOULD_CONTINUE);
                Log.w(e.device().getName_override() + " ReconnectEvent: persistIf() - short term (" + r + ")");
            }
            return Please.persistIf(e.totalTimeReconnecting().lt(m_timeout__SHORT_TERM__SHOULD_CONTINUE));
        }
        else
        {

            if (shouldLogMessage(lastLongTermLogDate, e.device().getName_override())) {
                lastLongTermLogDate.put(e.device().getName_override(), new Date());
                boolean r = e.totalTimeReconnecting().lt(m_timeout__LONG_TERM__SHOULD_CONTINUE);
                Log.w(e.device().getName_override() + " ReconnectEvent: persistIf() - long term (" + r + ")");
            }
            return Please.persistIf(e.totalTimeReconnecting().lt(m_timeout__LONG_TERM__SHOULD_CONTINUE));
        }
    }

    private boolean shouldLogMessage(HashMap<String, Date> lastLogDate, String serialNumber) {
        return shouldLogMessage(lastLogDate, serialNumber, 10000);
    }

    private boolean shouldLogMessage(HashMap<String, Date> lastLogDate, String serialNumber, Integer limit) {
        Date date = lastLogDate.get(serialNumber);
        if (date == null) {
            return true;
        }

        Date now = new Date();
        return (now.getTime() - date.getTime()) > limit;
    }
}
