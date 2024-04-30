package io.blustream.sulley.routines;

import io.blustream.sulley.routines.transactions.BlinkTransaction;
import io.blustream.sulley.routines.transactions.CheckEditableSettingsTransaction;
import io.blustream.sulley.routines.transactions.RealtimeTransaction;

public interface V1Routine extends Routine<V1Routine.Listener>,
        Editable<V1Routine.Listener, V1Routine.Settings>, Blink, Realtime.HumidTemp,
        Realtime.Battery {

    interface BleDefinitions extends CheckEditableSettingsTransaction.BleDefinitions,
            RealtimeTransaction.BleDefinitions, BlinkTransaction.BleDefinitions,
            BatterySubroutine.BleDefinitions, StatusSubroutine.BleDefinitions,
            V1DataSubroutine.BleDefinitions, RegistrationRoutine.BleDefinitions {
    }

    interface Listener extends Routine.Listener, Editable.Listener, V1DataSubroutine.Listener,
            BatterySubroutine.Listener, StatusSubroutine.Listener {
    }

    interface Settings extends Editable.Settings, CheckEditableSettingsTransaction.Settings {

    }
}
