package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import io.blustream.sulley.mappers.exceptions.MapperException;

public class V1DateMapper implements ByteMapper<Date> {
    @NonNull
    @Override
    public Date fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        // Calculate time
        Date date = new Date();

        long time_us = 0; // units don't make sense!

        for (int i = 5; i >= 0; i--) {
            time_us = time_us << 8;
            time_us |= (bytes[i] & 0xff);
        }

        date.setTime(date.getTime() - time_us / 1000);

        Calendar pastLimit = Calendar.getInstance();
        pastLimit.add(Calendar.YEAR, -1);

        Calendar futureLimit = Calendar.getInstance();
        futureLimit.add(Calendar.DAY_OF_YEAR, 1);

        if (date.before(pastLimit.getTime()) || date.after(futureLimit.getTime())) {
            throw new MapperException.InvalidDate();
        }
        return date;
    }

    @NonNull
    @Override
    public byte[] toBytes(Date object) throws MapperException {
        return new byte[0];
    }

    @Override
    public Short expectedReadLength() {
        return 6;
    }

    @Override
    public Short expectedWriteLength() {
        return null;
    }
}
