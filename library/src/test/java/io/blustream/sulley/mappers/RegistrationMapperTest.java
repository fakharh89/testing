package io.blustream.sulley.mappers;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/15/19.
 */
public class RegistrationMapperTest extends BaseTest<Integer, RegistrationMapper> {

    private final Integer realValidInteger = 1073635363;
    private final byte[] realValidArray = {63, -2, 96, 35};

    @Before
    @Override
    public void setUp() {
        mapper = new RegistrationMapper();
    }

    @Test
    public void fromBytesRandomArrayTest() {
        Integer integer = fromBytes(SamplesGenerator.generateRandomArray(SamplesGenerator.generateRandomByte()));
        assertNotNull(integer);
    }

    @Test
    public void toBytesValidTest() {
        byte[] bytes = toBytes(realValidInteger);
        validateRealArray(bytes);
    }

    @Test
    public void fromBytesLoadTest() {
        List<Integer> validSamples = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validSamples.add(mapper.fromBytes(SamplesGenerator.generateRandomArray(SamplesGenerator.generateRandomByte()))); //todo: check array size into mapper
            } catch (MapperException ex) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
        }
        for (Integer integer : validSamples) {
            assertNotNull(integer);
        }
        logLoadTest(validSamples,invalidArrays);
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
            assertNotNull(array);
        }
        System.out.println("validSamples.size() =  " + validArrays.size());
        System.out.println("invalid integers quantity = " + invalidIntegers);
        System.out.println("validArrays: " + validArrays);
        assertEquals(validArrays.size() + invalidIntegers, ITERATIONS_QUANTITY);
    }

    private void validateRealArray(byte[] bytes) {
        assertNotNull(bytes);
        assertArrayEquals(bytes, realValidArray);
    }

}
