package io.blustream.sulley.mappers;


import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;
import io.blustream.sulley.models.BatterySample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/10/19.
 */
public class BatteryMapperTest extends BaseTest<BatterySample, BatteryMapper> {

    private final byte[] VALID_ARRAY = {100};
    private final Integer VALID_VALUE = 100;
    private final byte[] INVALID_ARRAY = {101, 0};

    @Override
    public void setUp() {
        mapper = new BatteryMapper();
    }

    @Test
    public void fromBytesValidTest() {
        BatterySample batterySample = fromBytes(VALID_ARRAY);
        validateSample(batterySample);
        assertEquals(batterySample.getLevel(), VALID_VALUE);
    }

    @Test
    public void fromBytesInvalidTest() {
        BatterySample batterySample = fromBytes(INVALID_ARRAY);
        assertNull(batterySample);

    }

    @Test
    public void toBytesTestValid() {
        byte[] bytes = toBytes(fromBytes(VALID_ARRAY));
        assertNull(bytes);
    }

    @Test
    public void toBytesTestInvalid() {
        byte[] bytes = toBytes(fromBytes(INVALID_ARRAY));
        assertNull(bytes);
    }

    @Test
    public void fromBytesLoadTest() {
        List<BatterySample> validSamples = new LinkedList<>();
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
        for (BatterySample sample : validSamples) {
            validateSample(sample);
        }
        logLoadTest(validSamples, invalidArrays);
        assertEquals(validSamples.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    private void validateSample(BatterySample sample) {
        assertTrue(sample.getLevel() > 0 && sample.getLevel() <= 100);
    }
}
