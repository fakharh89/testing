package io.blustream.sulley.mappers;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/12/19.
 */
public class HumidTempSamplingIntervalMapperTest extends BaseTest<Integer, HumidTempSamplingIntervalMapper> {

    private final int ITERATIONS_QUANTITY = 1_000_000;

    private final byte[] realValidArray = {-22, 6, 0, 0};
    private final byte[] invalidArray = {-22, 6, 0, 0, 0};
    private final Integer valid_VALUE = 1770;

    @Before
    public void setUp() {
        mapper = new HumidTempSamplingIntervalMapper();
    }

    @Test
    public void fromBytesValidTest() {
        Integer interval = fromBytes(realValidArray);
        validateInterval(interval);
        assertNotNull(interval);
        assertEquals(interval, valid_VALUE);
    }

    @Test
    public void fromBytesInvalidTest() {
        Integer interval = fromBytes(invalidArray);
        assertNull(interval);
    }

    @Test
    public void fromBytesNullTest() {
        Integer interval = fromBytes(null);
        assertNull(interval);
    }

    public void toBytesNullTest() {
        toBytes(null);

    }

    @Test
    public void toBytesValidTest() {
        byte[] bytes = null;
        try {
            bytes = mapper.toBytes(valid_VALUE);
        } catch (MapperException e) {
            e.printStackTrace();
        }
        assertNotNull(bytes);
        assertArrayEquals(bytes, realValidArray);
    }


    @Test
    public void fromBytesLoadTest() {
        List<Integer> validDates = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validDates.add(mapper.fromBytes(
                        SamplesGenerator.generateRandomArray(SamplesGenerator.randBetween(mapper.expectedReadLength() - 1, mapper.expectedReadLength() + 1))));
            } catch (MapperException e) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
        }
        for (Integer date : validDates) {
            validateInterval(date);
        }
        logLoadTest(validDates,invalidArrays);
        assertEquals(validDates.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    @Test
    public void toBytesLoadTest() {
        List<byte[]> validDates = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validDates.add(mapper.toBytes(SamplesGenerator.generateRandomInteger()));
            } catch (MapperException e) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
        }
        for (byte[] date : validDates) {
            validateBytes(date);
        }
        System.out.println("valid dates.size() =  " + validDates.size());
        System.out.println("invalid arrays quantity = " + invalidArrays);
        assertEquals(validDates.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    private void validateInterval(Integer interval) {
        assertNotNull(interval);
    }

}
