package io.blustream.sulley.mappers;

import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;
import io.blustream.sulley.models.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/15/19.
 */
public class StatusMapperTest extends BaseTest<Status, StatusMapper> {

    private final byte[] realValidArray = {-112};
    private final byte[] invalidArray = {-112, 0};
    private final Status validStatus = new Status(new Date(), realValidArray[0]);

    @Override
    public void setUp() {
        mapper = new StatusMapper();
    }

    @Test
    public void fromBytesValidTest() {
        Status status = fromBytes(realValidArray);
        assertNotNull(status);
    }

    @Test
    public void fromBytesInvalidTest() {
        Status status = fromBytes(invalidArray);
        assertNull(status);
    }

    @Test
    public void toBytesValidTest() {
        byte[] bytes = toBytes(validStatus);
        assertNull(bytes);
    }

    @Test
    public void toBytesInvalidTest() {
        byte[] bytes = toBytes(null);
        assertNull(bytes);
    }

    @Test
    public void fromBytesLoadTest() {
        List<Status> validSamples = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validSamples.add(mapper.fromBytes(SamplesGenerator.generateRandomArray(
                        SamplesGenerator.randBetween(mapper.expectedReadLength() - 1, mapper.expectedReadLength() + 1))));
            } catch (MapperException ex) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
        }
        for (Status status : validSamples) {
            assertNotNull(status);
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
                validArrays.add(mapper.toBytes(new Status(SamplesGenerator.generateRandomDate(), SamplesGenerator.generateRandomByte())));
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
        System.out.println("validSamples.size() =  " + validArrays.size());
        System.out.println("invalid integers quantity = " + invalidIntegers);
        System.out.println("validArrays: " + validArrays);
        assertEquals(validArrays.size() + invalidIntegers, ITERATIONS_QUANTITY);
    }

    private void validateArray(byte[] array) {
        assertNotNull(array);
        assertEquals(array.length, (int) mapper.expectedWriteLength());
    }
}
