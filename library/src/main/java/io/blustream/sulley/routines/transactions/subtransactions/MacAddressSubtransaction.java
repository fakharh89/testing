package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import io.blustream.sulley.mappers.MacAddressMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.otau.model.PsKeyDatabase;

public abstract class MacAddressSubtransaction extends OTAUPropertySubtransaction {
    public MacAddressSubtransaction(@NonNull BleDefinitions definitions, PsKeyDatabase psKeyDatabase) {
        super(definitions, psKeyDatabase);
    }

    @Override
    public Integer getPropertyId() {
        return 1;
    }

    public String getMacAddress() throws MapperException {
        MacAddressMapper mapper = new MacAddressMapper();
        return mapper.fromBytes(getResponseValue());
    }
}
