package io.blustream.sulley.mappers;

import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;
import io.blustream.sulley.models.V4ManufacturerData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/11/19.
 */
public class V4ManufacturerDataMapperTest {

    private final int ITERATIONS_QUANTITY = 1_000_000;

    private final V4ManufacturerDataMapper mapper = new V4ManufacturerDataMapper();
    private final byte[] realValidArray = {66, 71, -19, 0, 69, 0, 69, 0, 69};
    private final byte[] invalidArray = {66, 71, -19, 0, 69, 0, 69, 0, 69, 0};

    @Test
    public void fromBytesValidTest() {
        V4ManufacturerData data = fromBytes(realValidArray);
        validateManufacturerData(data);
    }

    @Test
    public void fromBytesInvalidTest() {
        V4ManufacturerData data = fromBytes(invalidArray);
        assertNull(data);
    }

    @Test
    public void fromBytesLoadTest() {
        List<V4ManufacturerData> validDates = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validDates.add(mapper.fromBytes(SamplesGenerator.generateRandomDate(),
                        SamplesGenerator.generateRandomArray(SamplesGenerator.randBetween(mapper.expectedReadLength() - 1, mapper.expectedReadLength() + 1))));
            } catch (MapperException e) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());

            }
        }
        for (V4ManufacturerData date : validDates) {
            validateManufacturerData(date);
        }
        System.out.println("valid dates.size() =  " + validDates.size());
        System.out.println("invalid arrays quantity = " + invalidArrays);
        assertEquals(validDates.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    private void validateManufacturerData(V4ManufacturerData v4ManufacturerData) {
        assertNotNull(v4ManufacturerData);
        assertNotNull(v4ManufacturerData.getSerialNumber());
        assertNotNull(v4ManufacturerData.getDate());
        assertNotNull(v4ManufacturerData.getHumidTempSample());
        assertNotNull(v4ManufacturerData.getStatus());
    }

    private V4ManufacturerData fromBytes(byte[] bytes) {
        V4ManufacturerData data = null;
        try {
            data = mapper.fromBytes(new Date(), bytes);
        } catch (MapperException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());

        }
        return data;
    }
}
