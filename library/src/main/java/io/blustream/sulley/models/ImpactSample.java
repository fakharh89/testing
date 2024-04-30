package io.blustream.sulley.models;

import android.annotation.SuppressLint;

import java.util.Date;

import androidx.annotation.NonNull;

public class ImpactSample extends AbstractSample {
    private Float mX = 0f;
    private Float mY = 0f;
    private Float mZ = 0f;

    private Float mMagnitude;

    public ImpactSample(Date date, @NonNull Float x, @NonNull Float y, @NonNull Float z) {
        super(date);

        mX = x;
        mY = y;
        mZ = z;

        float squaredSum = mX * mX + mY * mY + mZ * mZ;
        mMagnitude = (float) Math.sqrt(squaredSum);
    }

    public Float getX() {
        return mX;
    }

    public Float getY() {
        return mY;
    }

    public Float getZ() {
        return mZ;
    }

    public Float getMagnitude() {
        return mMagnitude;
    }

    @Override
    @SuppressLint("DefaultLocale") // Just a logging message is used here
    public String toString() {
        return super.toString()
                + String.format(" Vector: (%f, %f, %f) Magnitude: %2.2f", mX, mY, mZ, mMagnitude);
    }
}
