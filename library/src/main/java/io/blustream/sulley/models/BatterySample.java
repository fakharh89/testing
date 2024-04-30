package io.blustream.sulley.models;

import java.util.Date;

public class BatterySample extends AbstractSample {
    private Integer mLevel;

    public BatterySample(Date date, Integer level) {
        super(date);

        this.mLevel = level;
    }

    public Integer getLevel() {
        return mLevel;
    }

    @Override
    public String toString() {
        return super.toString() + " Level: " + mLevel + "%";
    }
}
