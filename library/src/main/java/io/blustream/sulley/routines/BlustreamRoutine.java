package io.blustream.sulley.routines;

import io.blustream.sulley.routines.transactions.BlinkTransaction;
import io.blustream.sulley.routines.transactions.BufferDumpTransaction;
import io.blustream.sulley.routines.transactions.CheckEditableSettingsTransaction;
import io.blustream.sulley.routines.transactions.RealtimeTransaction;
import io.blustream.sulley.routines.transactions.SyncTimeTransaction;

public interface BlustreamRoutine<T extends BlustreamRoutine.Listener & Editable.Listener, U extends BlustreamRoutine.Settings>
        extends Routine<T>, Editable<T, U>, Blink, Realtime.HumidTemp, Realtime.Battery {

    interface BleDefinitions extends BufferDumpTransaction.BleDefinitions,
            CheckEditableSettingsTransaction.BleDefinitions, SyncTimeTransaction.BleDefinitions,
            RealtimeTransaction.BleDefinitions, BlinkTransaction.BleDefinitions,
            BatterySubroutine.BleDefinitions, StatusSubroutine.BleDefinitions {
    }

    interface Options {
        boolean succeedOnDisconnectAfterDelete();

        boolean editSettingsBeforeGettingData();
    }

    interface Listener extends Routine.Listener, Editable.Listener, BufferDumpTransaction.Listener,
            BatterySubroutine.Listener, StatusSubroutine.Listener {

    }

    interface Settings extends Editable.Settings, CheckEditableSettingsTransaction.Settings {

    }
}
