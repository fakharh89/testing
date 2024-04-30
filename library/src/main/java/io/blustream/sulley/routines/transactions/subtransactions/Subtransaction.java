package io.blustream.sulley.routines.transactions.subtransactions;

import com.idevicesinc.sweetblue.BleDevice;

public interface Subtransaction {
    void setDelegate(SubtransactionDelegate delegate);

    // These are syntax helpers to make routines feel like transactions
    boolean start();
    void onEnd(boolean success, boolean cancelPending);

    void succeed();
    void succeed(boolean cancelPending);
    void fail();
    void fail(boolean cancelPending);

//    void read(BleRead read);
//    void write(BleWrite write);
//    void enableNotify(BleNotify notify);

    BleDevice getDevice();
}

