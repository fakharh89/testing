package io.blustream.sulley.mappers;


import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;
import io.blustream.sulley.models.HumidTempSample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 6/27/19.
 */
public class HumidTempMapperTest extends BaseTest<HumidTempSample, HumidTempMapper> {

    private final int ITERATIONS_QUANTITY = 1_000_000;
    private final byte[] realValidArray = {-84, -78, -15, 18, 99, 1, -87, 26, 110, 10};
    private final byte[] invalidArray = {-84, -78, -15, 18, 99, 1, -87, 26, 110, 10, 0};
    private final byte[] realArray = {59, -95, -112, 84, 99, 1, -6, 16, -31, 9,};
    private final Float expectedRealHumidity = 43.46f;
    private final Float expectedRealTemperature = 25.29f;

    @Override
    public void setUp() {
        mapper = new HumidTempMapper();
    }

    @Test
    public void fromBytesInvalidTest() {
        HumidTempSample sample = fromBytes(invalidArray);
        assertNull(sample);
    }

    @Test
    public void fromBytesTestValidArray() {
        HumidTempSample sample = fromBytes(realValidArray);
        validateSample(sample);
    }

    @Test
    public void fromBytesLoadTest() {
        List<HumidTempSample> validSamples = new LinkedList<>();
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
        for (HumidTempSample sample : validSamples) {
            validateSample(sample);
        }
        logLoadTest(validSamples, invalidArrays);
        assertEquals(validSamples.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    @Test
    public void toBytesValidTest() {
        HumidTempSample sample = fromBytes(realValidArray); //todo create sample not via mapper.
        byte[] result = toBytes(sample);
        assertNull(result);
    }

    @Test
    public void toBytesInValidSampleTest() {
        HumidTempSample sample = fromBytes(invalidArray);  //todo create sample not via mapper.
        byte[] result = toBytes(sample);
        assertNull(result);
    }

    @Test
    public void testRealArray() {
        HumidTempSample sample = fromBytes(realArray);
        assertEquals(sample.getHumidity(), expectedRealHumidity);
        assertEquals(sample.getTemperature(), expectedRealTemperature);
    }

    private void validateSample(HumidTempSample sample) {
        assertNotNull(sample);
        assertTrue(isHumidityValid(sample.getHumidity()));
        assertTrue(isTemperatureValid(sample.getTemperature()));
    }

    private boolean isTemperatureValid(float temperature) {
        return ((temperature >= -50) && (temperature <= 150));
    }

    private boolean isHumidityValid(float humidity) {
        return ((humidity >= 0) && (humidity <= 100));
    }
}
