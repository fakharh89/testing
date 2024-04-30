package io.blustream.sulley.mappers;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;
import io.blustream.sulley.models.ImpactSample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/12/19.
 */
public class ImpactMapperTest extends BaseTest<ImpactSample, ImpactMapper> {

    private final byte[] realValidArray = {15, 43, 42, 95, 99, 1, 81, 0, 64, 0, 91, -2};
    private final float magnitudeSample = 13.5460005f;
    private final byte[] invalidArray = {15, 43, 42, 95, 99, 1, 81, 0, 64, 0, 91, -2, 0};

    @Before
    @Override
    public void setUp() {
        mapper = new ImpactMapper();
    }

    @Test
    public void fromBytesValidTest() {
        ImpactSample impactSample = fromBytes(realValidArray);
        validateImpact(impactSample);
        assertEquals(impactSample.getMagnitude(), (Float) magnitudeSample);
    }

    @Test
    public void fromBytesInvalidTest() {
        ImpactSample impactSample = fromBytes(invalidArray);
        assertNull(impactSample);
    }

    @Test
    public void fromBytesLoadTest() {
        List<ImpactSample> validSamples = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validSamples.add(mapper.fromBytes(SamplesGenerator.generateRandomArray(realValidArray.length)));
            } catch (MapperException e) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());

            }
        }
        for (ImpactSample sample : validSamples) {
            validateImpact(sample);
        }
        logLoadTest(validSamples,invalidArrays);
        assertEquals(validSamples.size() + invalidArrays, ITERATIONS_QUANTITY);
    }


    private void validateImpact(ImpactSample sample) {
        assertNotNull(sample);
        assertTrue(sample.getMagnitude() != 0);

    }
}
