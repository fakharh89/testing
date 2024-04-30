package io.blustream.sulley.otau.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PsKeysDetails {

    @SerializedName("NAME")
    private String name;
    @SerializedName("LENGTH")
    private String length;
    @SerializedName("ID")
    private String id;
    @SerializedName("OFFSET")
    private String offset;
    @SerializedName("DEFAULTVALUE")
    private List<String> defaultValues = new ArrayList<>();

    public String getName() {
        return name;
    }

    public int getLength() {
        return Integer.valueOf(length);
    }

    public int getId() {
        return Integer.valueOf(id);
    }

    public int getOffset() {
        return Integer.valueOf(offset);
    }

    public List<String> getDefaultValues() {
        return defaultValues;
    }
}
