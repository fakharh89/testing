package io.blustream.sulley.utilities;

import com.idevicesinc.sweetblue.BleTransaction;

/**
 * Created by Ruzhitskii Sviatoslav on 11/18/19.
 */
public interface TransactionsFinishListener {
    void onTransactionFinished(BleTransaction transaction);
}
