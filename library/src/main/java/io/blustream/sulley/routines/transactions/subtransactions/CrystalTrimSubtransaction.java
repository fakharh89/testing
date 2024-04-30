package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import io.blustream.sulley.mappers.CrystalTrimMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.otau.model.PsKeyDatabase;

public abstract class CrystalTrimSubtransaction extends OTAUPropertySubtransaction {
    public CrystalTrimSubtransaction(@NonNull BleDefinitions definitions, @NonNull PsKeyDatabase psKeyDatabase) {
        super(definitions, psKeyDatabase);
    }

    @Override
    public Integer getPropertyId() {
        return 2;
    }

    public Integer getCrystalTrim() throws MapperException {
        CrystalTrimMapper mapper = new CrystalTrimMapper();
        return mapper.fromBytes(getResponseValue());
    }
}
