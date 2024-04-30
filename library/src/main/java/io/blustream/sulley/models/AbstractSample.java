package io.blustream.sulley.models;

import java.util.Date;

abstract class AbstractSample implements Sample {
    protected Date mDate;

    public Date getDate() {
        return mDate;
    }

    AbstractSample(Date date) {
        this.mDate = date;
    }

    @Override
    public String toString() {
        return "Date: " + mDate;
    }
}