package io.blustream.sulley.mappers.generators;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by Ruzhitskii Sviatoslav on 6/27/19.
 */
public class SamplesGenerator {

    private static long YEAR_SEC = 60 * 60 * 24 * 365;

    public static byte[] generateRandomArray(int size) {
        byte[] bytes = new byte[size];
        new Random().nextBytes(bytes);
        return bytes;
    }

    public static Integer generateRandomInteger() {
        return new Random().nextInt(10000);
    }

    public static Byte generateRandomByte() {
        return (byte) Math.abs(new Random().nextInt(Byte.MAX_VALUE + 1));
    }

    public static Float generateRandomFloat() {
        return new Random().nextFloat();
    }

    public static Date generateRandomDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, randBetween(-YEAR_SEC, YEAR_SEC));
        return calendar.getTime();
    }

    public static int randBetween(long start, long end) {
        return (int) ((int) start + Math.round(Math.random() * (end - start)));
    }
}
