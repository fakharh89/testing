package io.blustream.sulley.mappers;


import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;
import io.blustream.sulley.models.AccelerometerMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/9/19.
 */
public class AccelerometerModeMapperTest extends BaseTest<AccelerometerMode, AccelerometerModeMapper> {

    private final byte[] VALID_ARRAY = {2};
    private final byte[] INVALID_ARRAY = {5, 0};
    private final int MOTION_MODE_VALUE = 2;

    @Override
    public void setUp() {
        mapper = new AccelerometerModeMapper();
    }

    @Test
    public void fromBytesTestValid() {
        AccelerometerMode sample = fromBytes(VALID_ARRAY);
        validateMode(sample);
    }

    @Test
    public void fromBytesTestInvalid() {
        AccelerometerMode sample = fromBytes(INVALID_ARRAY);
        assertNull(sample);
    }

    @Test
    public void toBytesTestValid() {
        AccelerometerMode mode = AccelerometerMode.MOTION;
        byte[] bytes = toBytes(mode);
        assertNotNull(bytes);
        assertEquals(bytes.length, VALID_ARRAY.length);
        assertEquals(MOTION_MODE_VALUE, bytes[0]);
    }

    @Test
    public void toBytesTestInvalid() {
        AccelerometerMode mode = AccelerometerMode.UNKNOWN;
        byte[] bytes = toBytes(mode);
        assertNull(bytes);
    }

    @Test
    public void fromBytesLoadTest() {
        List<AccelerometerMode> validModes = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validModes.add(mapper.fromBytes(SamplesGenerator.generateRandomArray(VALID_ARRAY.length)));
            } catch (MapperException ex) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
        }
        for (AccelerometerMode mode : validModes) {
            validateMode(mode);
        }
        logLoadTest(validModes, invalidArrays);
        assertEquals(validModes.size() + invalidArrays, ITERATIONS_QUANTITY);
    }



    private void validateMode(AccelerometerMode mode) {
        assertNotNull(mode);
        assertNotSame(mode, AccelerometerMode.UNKNOWN);
    }
}
