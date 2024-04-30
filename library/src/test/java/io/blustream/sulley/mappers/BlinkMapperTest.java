package io.blustream.sulley.mappers;


import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/10/19.
 */
public class BlinkMapperTest extends BaseTest<Integer, BlinkMapper> {

    private final byte[] VALID_ARRAY = {5};
    private final Integer VALID_VALUE = 5;
    private final byte[] INVALID_ARRAY = {5, 0};

    @Before
    @Override
    public void setUp() {
        mapper = new BlinkMapper();
    }

    @Test
    public void fromBytesValidTest() {
        Integer count = fromBytes(VALID_ARRAY);
        assertNull(count);
    }

    @Test
    public void fromBytesInvalidTest() {
        Integer count = fromBytes(INVALID_ARRAY);
        assertNull(count);
    }

    @Test
    public void toBytesTestValid() {
        byte[] bytes = toBytes(VALID_VALUE);
        assertNotNull(bytes);
        assertEquals(bytes.length, VALID_ARRAY.length);
        assertEquals(bytes[0], VALID_VALUE.byteValue());
    }

    @Test
    public void toBytesTestInvalid() {
        byte[] bytes = toBytes(fromBytes(INVALID_ARRAY));
        assertNull(bytes);
    }

    @Test
    public void fromBytesLoadTest() {
        List<Integer> validSamples = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validSamples.add(mapper.fromBytes(SamplesGenerator.generateRandomArray(VALID_ARRAY.length)));
            } catch (MapperException ex) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());

            }
        }
        for (Integer sample : validSamples) {
            validateSample(sample);
        }
        System.out.println("validSamples.size() =  " + validSamples.size());
        System.out.println("invalid arrays quantity = " + invalidArrays);
        System.out.println("validModes: " + validSamples);
        assertEquals(validSamples.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    @Test
    public void toBytesLoadTest() {
        List<byte[]> validArrays = new LinkedList<>();
        int invalidIntegers = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validArrays.add(mapper.toBytes(SamplesGenerator.generateRandomInteger()));
            } catch (MapperException ex) {
                invalidIntegers++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());

            }
        }
        for (byte[] array : validArrays) {
            validateArray(array);
        }
        logLoadTest(validArrays,invalidIntegers);
        assertEquals(validArrays.size() + invalidIntegers, ITERATIONS_QUANTITY);
    }

    private void validateSample(Integer count) {
        assertNotNull(count);
        assertTrue(count > 0);
    }

    private void validateArray(byte[] bytes) {
        assertNotNull(bytes);
        assertEquals(bytes.length, VALID_ARRAY.length);
    }
}
