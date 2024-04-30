package io.blustream.sulley.routines.transactions.subtransactions;

import com.idevicesinc.sweetblue.BleDevice;

// TODO Write adapter pattern for transactions and subtransactions since they are so similar
public interface SubtransactionDelegate {
    void ended(Subtransaction subtransaction, boolean success, boolean cancelPending);
    BleDevice getDevice(Subtransaction subtransaction);
}

/*
TODO Add all of these functions
protected abstract void start();

protected void update(double var1);

protected void onEnd(BleTransaction.EndReason var1);

protected BleTransaction.Atomicity getAtomicity();

public BleDevice getDevice();

public boolean isRunning();

public void cancel();

public boolean fail();

public boolean succeed();

public double getTime();

public final ReadWriteEvent read(BleRead var1);

public final Void readMany(BleRead[] var1);

public final Void readMany(Iterable<BleRead> var1);

public final ReadWriteEvent readBatteryLevel(ReadWriteListener var1);

public final Void enableNotifies(BleNotify[] var1);

public final Void enableNotifies(Iterable<BleNotify> var1);

public final ReadWriteEvent enableNotify(BleNotify var1);

public final ReadWriteEvent disableNotify(BleNotify var1);

public final Void disableNotifies(BleNotify[] var1);

public final Void disableNotifies(Iterable<BleNotify> var1);

public final ReadWriteEvent write(BleWrite var1);

public final ReadWriteEvent write(BleWrite var1, ReadWriteListener var2);

public final ReadWriteEvent write(BleDescriptorWrite var1);

public final ReadWriteEvent write(BleDescriptorWrite var1, ReadWriteListener var2);

public final ReadWriteEvent read(BleDescriptorRead var1);

public final ReadWriteEvent readRssi();

public final ReadWriteEvent readRssi(ReadWriteListener var1);

public final ReadWriteEvent setConnectionPriority(BleConnectionPriority var1);

public final ReadWriteEvent setConnectionPriority(BleConnectionPriority var1, ReadWriteListener var2);

@Nullable(Prevalence.NEVER)
public final ReadWriteEvent negotiateMtuToDefault();

public final ReadWriteEvent negotiateMtuToDefault(ReadWriteListener var1);

public final ReadWriteEvent negotiateMtu(int var1);

public final ReadWriteEvent negotiateMtu(int var1, ReadWriteListener var2);

public final ReadWriteEvent setPhyOptions(Phy var1);

public final ReadWriteEvent setPhyOptions(Phy var1, ReadWriteListener var2);

public final ReadWriteEvent readPhyOptions();

public final ReadWriteEvent readPhyOptions(ReadWriteListener var1);
 */