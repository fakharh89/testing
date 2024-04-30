package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import io.blustream.sulley.mappers.exceptions.MapperException;

public class DateMapper implements ByteMapper<Date> {
    public Date fromBytes(byte[] bytes) throws MapperException {
        checkReadLength(bytes);

        // Calculate time
        Date date = new Date();

        long time_us = 0;

        for (int i = 5; i >= 0; i--) {
            time_us = time_us << 8;
            time_us |= (bytes[i] & 0xff);
        }

        date.setTime(time_us * 1024 / 1000);

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
    public byte[] toBytes(Date date) throws MapperException {
        checkNotNull(date);
        long timeInMilliseconds = date.getTime() * 1000 / 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putLong(timeInMilliseconds);
        byte[] bytes = byteBuffer.array();
        byte[] result = Arrays.copyOfRange(bytes, 0, 6);
        //todo: make future/past limit check.

        return result;
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
