package io.blustream.sulley.routines;

import io.blustream.sulley.routines.transactions.RegistrationTransaction;

public interface RegistrationRoutine extends Routine<RegistrationRoutine.Listener> {
    interface Listener extends Routine.Listener {

        void onRegistrationSuccess();

        void onRegistrationFailure();
    }

    interface BleDefinitions extends RegistrationTransaction.BleDefinitions {}
}
