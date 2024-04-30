package io.blustream.sulley.models;

import java.util.Date;
import java.util.Objects;

public class MotionSample extends AbstractSample {
    private Boolean mMoving;

    public MotionSample(Date date, Boolean moving) {
        super(date);
        mMoving = moving;
    }

    public Boolean isMoving() {
        return mMoving;
    }

    @Override
    public String toString() {
        return super.toString() + " Moving: " + mMoving;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MotionSample)) return false;
        MotionSample that = (MotionSample) o;
        return Objects.equals(mMoving, that.mMoving);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mMoving);
    }
}
