package io.blustream.sulley.mappers;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.blustream.sulley.mappers.exceptions.MapperException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Ruzhitskii Sviatoslav on 7/12/19.
 */
public abstract class BaseTest<S, M extends ByteMapper<S>> {

    protected final int ITERATIONS_QUANTITY = 100_000;

    protected M mapper;

    @Before
    public abstract void setUp();

    @Test
    public void toBytesNullTest() {
        byte[] result = toBytes(null);
        assertNull(result);
    }

    @Test
    public void fromBytesNullTest() {
        S result = fromBytes(null);
        assertNull(result);
    }

    protected byte[] toBytes(S sample) {
        byte[] bytes = null;
        try {
            bytes = mapper.toBytes(sample);
        } catch (MapperException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        return bytes;
    }

    protected S fromBytes(byte[] bytes) {
        S sample = null;
        try {
            sample = mapper.fromBytes(bytes);
        } catch (MapperException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());

        }
        return sample;
    }

    void validateBytes(byte[] bytes) {
        assertNotNull(bytes);
        assertEquals(bytes.length, (short) mapper.expectedReadLength());
    }

    void logLoadTest(List validSamples, int inValidArrays) {
        System.out.println("valid samples quantity  =  " + validSamples.size());
        System.out.println("invalid arrays quantity = " + inValidArrays);
        System.out.println("validSamples: \n" + validSamples);
    }

}
