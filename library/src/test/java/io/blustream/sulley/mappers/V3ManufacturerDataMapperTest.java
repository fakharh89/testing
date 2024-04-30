package io.blustream.sulley.mappers;

import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;
import io.blustream.sulley.models.V3ManufacturerData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/11/19.
 */
public class V3ManufacturerDataMapperTest {

    private final int ITERATIONS_QUANTITY = 1_000_000;

    private final V3ManufacturerDataMapper mapper = new V3ManufacturerDataMapper();
    private final byte[] realValidArray = {66, 71, -19, 0, 69, 0, 69, 0, 69, 0};  // this data is real. The issue here that all 69 lvalues are not valid.
    private final byte[] invalidArray = {66, 71, -19, 0, 69, 0, 69, 0, 69, 0, 0};

    @Test
    public void fromBytesValidTest() {
        V3ManufacturerData data = fromBytes(realValidArray);
        validateManufacturerData(data);
    }

    @Test
    public void fromBytesInvalidTest() {
        V3ManufacturerData data = fromBytes(invalidArray);
        assertNull(data);
    }

    @Test
    public void fromBytesLoadTest() {
        List<V3ManufacturerData> validDates = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validDates.add(mapper.fromBytes(SamplesGenerator.generateRandomDate(),
                        SamplesGenerator.generateRandomArray(SamplesGenerator.randBetween(realValidArray.length - 1, realValidArray.length + 1))));
            } catch (MapperException e) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
        }
        for (V3ManufacturerData date : validDates) {
            validateManufacturerData(date);
        }
        System.out.println("valid dates.size() =  " + validDates.size());
        System.out.println("invalid arrays quantity = " + invalidArrays);
        assertEquals(validDates.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    private void validateManufacturerData(V3ManufacturerData v3ManufacturerData) {
        assertNotNull(v3ManufacturerData);
        assertNotNull(v3ManufacturerData.getSerialNumber());
        assertNotNull(v3ManufacturerData.getBatterySample());
        assertNotNull(v3ManufacturerData.getDate());
        assertNotNull(v3ManufacturerData.getHumidTempSample());
        assertNotNull(v3ManufacturerData.getStatus());
    }

    private V3ManufacturerData fromBytes(byte[] bytes) {
        V3ManufacturerData data = null;
        try {
            data = mapper.fromBytes(new Date(), bytes);
        } catch (MapperException invalidLength) {
            invalidLength.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());

        }
        return data;
    }
}
