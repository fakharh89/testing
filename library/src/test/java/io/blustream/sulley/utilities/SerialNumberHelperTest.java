package io.blustream.sulley.utilities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.text.TextUtils;
import android.util.SparseArray;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import io.blustream.logger.Log;
import io.blustream.sulley.sensor.DefaultSensorManagerConfig;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SerialNumberHelperTest {
    private final String[] mCompatibleSensorIdentifiers = {"01", "10", "02", "42"};

    @Before
    public void setup() {
        mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });

    }

    @Test
    public void getSerialFromMac_Test() {
        ScanResult mockScanResult = mock(ScanResult.class);
        ScanRecord mockScanRecord = mock(ScanRecord.class);
        BluetoothDevice mockBluetoothDevice = mock(BluetoothDevice.class);
        SparseArray mockSparseArray = mock(SparseArray.class);
        when(mockSparseArray.size()).thenReturn(0);
        when(mockBluetoothDevice.getAddress()).thenReturn("0C:1A:10:00:76:31");
        when(mockScanResult.getDevice()).thenReturn(mockBluetoothDevice);
        when(mockScanRecord.getDeviceName()).thenReturn("Blustream-OTA");
        when(mockScanRecord.getManufacturerSpecificData()).thenReturn(mockSparseArray);
        when(mockScanResult.getScanRecord()).thenReturn(mockScanRecord);
//        when(mockScanRecord.getServiceUuids()).thenReturn(null);

        String serialFromMac = new SerialNumberHelper(mCompatibleSensorIdentifiers).getSerialFromScanResult(mockScanResult);
        assertTrue(serialFromMac.equalsIgnoreCase("00763142"));

    }
}
