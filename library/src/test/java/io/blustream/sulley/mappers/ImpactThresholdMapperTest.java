package io.blustream.sulley.mappers;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


/**
 * Created by Ruzhitskii Sviatoslav on 7/12/19.
 */
public class ImpactThresholdMapperTest extends BaseTest<Float, ImpactThresholdMapper> {

    private final byte[] realValidArray = {110};
    private final byte[] invalidArray = {110, 0};
    private final float validThreshold = 6.875f;
    private final float invalidThreshold = 16f;

    @Before
    @Override
    public void setUp() {
        mapper = new ImpactThresholdMapper();
    }

    @Test
    public void fromBytesValidTest() {
        Float threshold = fromBytes(realValidArray);
        validateThreshold(threshold);
    }

    @Test
    public void fromBytesInvalidTest() {
        Float threshold = fromBytes(invalidArray);
        assertNull(threshold);
    }

    @Test
    public void toBytesValidTest() {
        byte[] bytes = toBytes(validThreshold);
        assertNotNull(bytes);
    }

    @Test
    public void toBytesInvalidTest() {
        byte[] bytes = toBytes(invalidThreshold);
        assertNull(bytes);
    }

    @Test
    public void fromBytesLoadTest() {
        List<Float> validSamples = new LinkedList<>();
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
        for (Float threshold : validSamples) {
            validateThreshold(threshold);
        }
        logLoadTest(validSamples, invalidArrays);
        assertEquals(validSamples.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    @Test
    public void toBytesLoadTest() {
        List<byte[]> validArrays = new LinkedList<>();
        int invalidIntegers = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validArrays.add(mapper.toBytes(SamplesGenerator.generateRandomFloat()));
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

    private void validateThreshold(Float threshold) {
        assertNotNull(threshold);
        ByteBuffer b = ByteBuffer.allocate(2);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putChar((char) (threshold / 0.0625));
        if ((threshold >= 16) || (threshold <= 0) || (b.getChar(0) >= 0xff)) {
            fail("wrong threshold " + threshold);
        }
    }

    private void validateArray(byte[] array) {
        assertNotNull(array);
        assertEquals(array.length, (int) mapper.expectedWriteLength());
    }
}
