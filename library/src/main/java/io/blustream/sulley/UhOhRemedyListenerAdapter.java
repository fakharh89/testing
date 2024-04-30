package io.blustream.sulley;

import com.idevicesinc.sweetblue.BleManager;

import androidx.annotation.Nullable;
import io.blustream.logger.Log;
import io.blustream.sulley.sensor.UhOhRemedyListener;

public class UhOhRemedyListenerAdapter implements BleManager.UhOhListener {

    @Nullable
    private UhOhRemedyListener mUhOhRemedyListener;

    public UhOhRemedyListenerAdapter(@Nullable UhOhRemedyListener uhOhRemedyListener) {
        this.mUhOhRemedyListener = uhOhRemedyListener;
    }

    @Override
    public void onEvent(UhOhEvent uhOhEvent) {
        Log.e("UhOh " + uhOhEvent.toString());
        determineRemedy(uhOhEvent.remedy());
    }

    private void determineRemedy(BleManager.UhOhListener.Remedy remedy) {
        if (mUhOhRemedyListener == null) {
            return;
        }

        switch (remedy) {
            // V3 only!
//            case RECYCLE_CONNECTION:
//                mUhOhRemedyListener.onRecycleConnection();
//                break;
            case WAIT_AND_SEE:
                mUhOhRemedyListener.onWaitAndSee();
                break;
            case RESET_BLE:
                mUhOhRemedyListener.onResetBle();
                break;
            case RESTART_PHONE:
                mUhOhRemedyListener.onRestartPhone();
                break;
        }
    }
}
