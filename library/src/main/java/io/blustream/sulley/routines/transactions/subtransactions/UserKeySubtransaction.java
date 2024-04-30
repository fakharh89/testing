package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import io.blustream.sulley.mappers.UserKeyMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.otau.model.PsKeyDatabase;

public abstract class UserKeySubtransaction extends OTAUPropertySubtransaction {
    public UserKeySubtransaction(@NonNull BleDefinitions definitions, @NonNull PsKeyDatabase psKeyDatabase) {
        super(definitions, psKeyDatabase);
    }

    @Override
    public Integer getPropertyId() {
        return 4;
    }

    public String getUserKeys() throws MapperException {
        UserKeyMapper mapper = new UserKeyMapper();
        return mapper.fromBytes(getResponseValue());
    }
}
