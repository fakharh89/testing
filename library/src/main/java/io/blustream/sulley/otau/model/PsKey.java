package io.blustream.sulley.otau.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PsKey {

    @SerializedName("BUILD_ID")
    private int buildId;
    @SerializedName("PSKEY")
    private List<PsKeysDetails> details = new ArrayList<>();

    public int getBuildId() {
        return buildId;
    }

    public List<PsKeysDetails> getDetails() {
        return details;
    }
}
