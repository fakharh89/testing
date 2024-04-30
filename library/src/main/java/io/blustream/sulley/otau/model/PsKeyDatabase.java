package io.blustream.sulley.otau.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PsKeyDatabase {

    @SerializedName("VERSION")
    private int version;
    @SerializedName("PSKEYS")
    private List<PsKey> psKeys = new ArrayList<>();

    public int getVersion() {
        return version;
    }

    public List<PsKey> getPsKeys() {
        return psKeys;
    }
}
