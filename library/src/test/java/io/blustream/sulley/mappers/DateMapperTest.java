package io.blustream.sulley.mappers;


import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.mappers.generators.SamplesGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 6/27/19.
 */
public class DateMapperTest extends BaseTest<Date, DateMapper> {

    private final byte[] validBytes = {-84, -78, -15, 18, 99, 1};
    private final byte[] invalidSizeBytes = {-84, -78, -15, 18, 99, 1, 0};
    private Calendar pastLimit, futureLimit;


    @Before
    @Override
    public void setUp() {
        mapper = new DateMapper();
        pastLimit = Calendar.getInstance();
        pastLimit.add(Calendar.YEAR, -1);
        futureLimit = Calendar.getInstance();
        futureLimit.add(Calendar.DAY_OF_YEAR, 1);
    }

    @Test
    public void fromBytesValidTest() {
        Date date = fromBytes(validBytes);
        validateDate(date);
    }

    @Test
    public void testInvalidSizeArray() {
        Date date = fromBytes(invalidSizeBytes);
        assertNull(date);
    }

    @Test
    public void toBytesValidTest() {
        Date date = Calendar.getInstance().getTime();
        byte[] result = toBytes(date);
        assertNotNull(result);
    }

    @Test
    public void testToBytesInvalidOldDate() {
        Calendar old = Calendar.getInstance();
        old.add(Calendar.YEAR, -1);
        Date date = old.getTime();
        assertNull(toBytes(date));
    }

    @Test
    public void testToBytesInvalidFutureDate() {
        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_YEAR, 2);
        Date date = future.getTime();
        assertNull(toBytes(date));
    }

    @Test
    public void fromBytesLoadTest() {
        List<Date> validDates = new LinkedList<>();
        int invalidArrays = 0;
        for (int i = 0; i < ITERATIONS_QUANTITY; i++) {
            try {
                validDates.add(mapper.fromBytes(SamplesGenerator.generateRandomArray(validBytes.length)));
            } catch (MapperException e) {
                invalidArrays++;
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
        }
        for (Date date : validDates) {
            validateDate(date);
        }
        logLoadTest(validDates, invalidArrays);
        assertEquals(validDates.size() + invalidArrays, ITERATIONS_QUANTITY);
    }

    private void validateDate(Date date) {
        assertNotNull(date);
        assertTrue(date.after(pastLimit.getTime()) && date.before(futureLimit.getTime()));
    }


}
