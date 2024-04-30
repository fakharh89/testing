package io.blustream.sulley.otau.model;

import com.google.gson.annotations.SerializedName;

public class PsKeyDatabaseObject {

    @SerializedName("PSKEY_DATABASE")
    private PsKeyDatabase psKeyDatabase;

    public PsKeyDatabase getPsKeyDatabase() {
        return psKeyDatabase;
    }
}
