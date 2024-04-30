package io.blustream.sulley.mappers;


import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;
import io.blustream.sulley.models.BlustreamManufacturerData;
import io.blustream.sulley.models.V3ManufacturerData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/11/19.
 */
public class BlustreamManufacturerDataMapperTest {

    private final int ITERATIONS_QUANTITY = 1_000_000;

    private final byte[] validArrayV3 = {66, 71, -19, 0, 69, 0, 69, 0, 69, 0};
    private final byte[] validArrayV4 = {66, 71, -19, 0, 69, 0, 69, 0, 69};
    private final byte[] invalidArray = {66, 71, -19, 0, 69, 0, 69, 0, 69, 0, 0};
    private final BlustreamManufacturerDataMapper mapper = new BlustreamManufacturerDataMapper();

    @Test
    public void fromBytesValidV3Test() {
        BlustreamManufacturerData data = fromBytes(validArrayV3);
        validateManufacturerData(data);
    }

    @Test
    public void fromBytesValidV4Test() {
        BlustreamManufacturerData data = fromBytes(validArrayV4);
        validateManufacturerData(data);
    }

    @Test
    public void fromBytesInvalidTest() {
        BlustreamManufacturerData data = fromBytes(invalidArray);
        assertNull(data);
    }

    @Test
    public void fromBytesLoadTest() {
        List<BlustreamManufacturerData> validDates = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validDates.add(mapper.fromBytes(SamplesGenerator.generateRandomDate(),
                        SamplesGenerator.generateRandomArray(SamplesGenerator.randBetween(validArrayV4.length - 1, validArrayV3.length + 1))));
            } catch (MapperException e) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());

            }
        }
        for (BlustreamManufacturerData date : validDates) {
            validateManufacturerData(date);
        }
        System.out.println("valid dates.size() =  " + validDates.size());
        System.out.println("invalid arrays quantity = " + invalidArrays);
        assertEquals(validDates.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    private BlustreamManufacturerData fromBytes(byte[] bytes) {
        BlustreamManufacturerData data = null;
        try {
            data = mapper.fromBytes(SamplesGenerator.generateRandomDate(), bytes);
        } catch (MapperException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void validateManufacturerData(BlustreamManufacturerData blustreamManufacturerData) {
        assertNotNull(blustreamManufacturerData);
        assertNotNull(blustreamManufacturerData.getSerialNumber());
        assertNotNull(blustreamManufacturerData.getDate());
        assertNotNull(blustreamManufacturerData.getHumidTempSample());
        assertNotNull(blustreamManufacturerData.getStatus());
        if (blustreamManufacturerData instanceof V3ManufacturerData) {
            assertNotNull(((V3ManufacturerData) blustreamManufacturerData).getBatterySample());
        }
    }

}
