package io.blustream.sulley.mappers;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;
import io.blustream.sulley.models.MotionSample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/12/19.
 */
public class MotionMapperTest extends BaseTest<MotionSample, MotionMapper> {

    private final byte[] realValidArray = {86, 21, 76, 109, 99, 1, 1};
    private final byte[] invalidArray = {86, 21, 76, 109, 99, 1, 1, 0};
    private final MotionSample validSample = new MotionSample(new Date(1563184223049L), true);

    @Before
    @Override
    public void setUp() {
        mapper = new MotionMapper();
    }

    @Test
    public void fromBytesValidTest() {
        MotionSample motionSample = fromBytes(realValidArray);
        System.out.println(motionSample.getDate().getTime());
        validateRealSample(motionSample);
    }

    @Test
    public void fromBytesInvalidTest() {
        MotionSample threshold = fromBytes(invalidArray);
        assertNull(threshold);
    }

    @Test
    public void toBytesValidTest() {
        byte[] bytes = toBytes(fromBytes(realValidArray));
        assertNull(bytes);
    }

    @Test
    public void fromBytesLoadTest() {
        List<MotionSample> validSamples = new LinkedList<>();
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
        for (MotionSample threshold : validSamples) {
            validateMotion(threshold);
        }
        logLoadTest(validSamples,invalidArrays);
        assertEquals(validSamples.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    private void validateMotion(MotionSample sample) {
        assertNotNull(sample);
    }

    private void validateRealSample(MotionSample motionSample) {
        assertNotNull(motionSample);
        assertEquals(motionSample.getDate(), validSample.getDate());
        assertEquals(motionSample.isMoving(), validSample.isMoving());
        assertEquals(motionSample, validSample);
    }

}
