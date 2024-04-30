package com.blustream.demo;

import org.junit.Test;

import io.blustream.sulley.models.VisibilityStatus;

import static org.junit.Assert.assertEquals;

public class VisibilityStatusTest {

    @Test
    public void testVisibility() {
        VisibilityStatus visibilityStatus = new VisibilityStatus(0);
        System.out.println("visibilityStatus = " + visibilityStatus);
        assertEquals("00000000", getBinaryReadableStatus(visibilityStatus.getStatus()));
        visibilityStatus.setIBeaconAdvertised(true);
        System.out.println("visibilityStatus = " + visibilityStatus);
        assertEquals("00000100", getBinaryReadableStatus(visibilityStatus.getStatus()));
        visibilityStatus.setIBeaconAdvertised(false);
        System.out.println("visibilityStatus = " + visibilityStatus);
        assertEquals("00000000", getBinaryReadableStatus(visibilityStatus.getStatus()));
        visibilityStatus.setIBeaconAdvertised(false);
        System.out.println("visibilityStatus = " + visibilityStatus);
        assertEquals("00000000", getBinaryReadableStatus(visibilityStatus.getStatus()));
    }

    private String getBinaryReadableStatus(int status) {
        return String.format("%8s", Integer.toBinaryString(status & 0xFF))
                .replace(' ', '0');
    }
}
