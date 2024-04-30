package io.blustream.sulley.models;

import java.util.Date;

public class HumidTempSample extends AbstractSample {
    private Float mHumidity;
    private Float mTemperature;

    public HumidTempSample(Date date, Float humidity, Float temperature) {
        super(date);
        mHumidity = humidity;
        mTemperature = temperature;
    }

    public Float getHumidity() {
        return mHumidity;
    }

    public Float getTemperature() {
        return mTemperature;
    }

    @Override
    public String toString() {
        return super.toString() + " Humidity: " + mHumidity
                + "%RH Temperature: " + mTemperature + "°C";
    }
}
