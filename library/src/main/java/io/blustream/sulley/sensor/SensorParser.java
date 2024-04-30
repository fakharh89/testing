package io.blustream.sulley.sensor;

import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.text.TextUtils;

import io.blustream.sulley.utilities.SerialNumberHelper;
import io.blustream.sulley.utilities.Uuids;

public class SensorParser {
    private final String[] mCompatibleDeviceIdentifiers;

    SensorParser(String[] compatibleDeviceIdentifiers) {
        mCompatibleDeviceIdentifiers = compatibleDeviceIdentifiers;
    }

    /**
     * This method will check all other methods for you, returning a parse result
     * indicating the type. Alternatively, you can check a ScanResult for a specific type
     * by calling the other methods directly.
     *
     * @param scanResult
     * @return SensorParseResult, or null if not able to parse.
     */
    public SensorParseResult parse(ScanResult scanResult) {
        SensorParseResult result;

        result = parseOTAUMode(scanResult);
        if (result != null) {
            return result;
        }

        result = parseIBeaconMode(scanResult);
        if (result != null) {
            return result;
        }

        result = parseV1(scanResult);
        if (result != null) {
            return result;
        }

        result = parseV3(scanResult);
        if (result != null) {
            return result;
        }

        result = parseV4(scanResult);

        return result; // If we got here, then it's either v4 or null, so just return result.

    }

    public SensorParseResult parseOTAUMode(ScanResult scanResult) {
        if (!isValidScanResult(scanResult)) {
            return null;
        }

        if ((scanResult.getScanRecord().getManufacturerSpecificData() == null ||
                scanResult.getScanRecord().getManufacturerSpecificData().size() == 0) &&
                !TextUtils.isEmpty(scanResult.getScanRecord().getDeviceName()) ) {

            return new SensorParseResult(SensorParseResult.SensorMode.OTAU,
                    scanResult.getDevice().getAddress(),
                    new SerialNumberHelper(mCompatibleDeviceIdentifiers).getSerialFromScanResult(scanResult));
        }

        return null;
    }

    public SensorParseResult parseIBeaconMode(ScanResult scanResult) {
        if (!isValidScanResult(scanResult)) {
            return null;
        }

        if (scanResult.getScanRecord().getServiceUuids() != null) {
            for (ParcelUuid uuid : scanResult.getScanRecord().getServiceUuids()) {
                if (uuid.getUuid().toString().equalsIgnoreCase(Uuids.IBeacon.IBEACON_SERVICE.toString())) {
                    return new SensorParseResult(SensorParseResult.SensorMode.IBEACON,
                            scanResult.getDevice().getAddress(),
                            new SerialNumberHelper(mCompatibleDeviceIdentifiers).getSerialFromScanResult(scanResult));

                }
            }
        }

        if (new IBeaconParser().isBlustreamIBeacon(scanResult.getScanRecord().getBytes())) {
            return new SensorParseResult(SensorParseResult.SensorMode.IBEACON,
                    scanResult.getDevice().getAddress(),
                    new SerialNumberHelper(mCompatibleDeviceIdentifiers).getSerialFromScanResult(scanResult));
        }

        return null;
    }

    public SensorParseResult parseV1(ScanResult scanResult) {
        if (!isValidScanResult(scanResult)) {
            return null;
        }

        if (scanResult.getScanRecord().getServiceUuids() != null) {
            for (ParcelUuid uuid : scanResult.getScanRecord().getServiceUuids()) {
                if (uuid.getUuid().toString().equalsIgnoreCase(Uuids.v1.BLUSTREAM_SERVICE.toString())) {
                    return new SensorParseResult(SensorParseResult.SensorMode.V1,
                            scanResult.getDevice().getAddress(),
                            new SerialNumberHelper(mCompatibleDeviceIdentifiers).getSerialFromScanResult(scanResult));
                }
            }
        }
        return null;
    }

    public SensorParseResult parseV3(ScanResult scanResult) {
        if (!isValidScanResult(scanResult)) {
            return null;
        }

        if (scanResult.getScanRecord().getServiceUuids() != null) {
            for (ParcelUuid uuid : scanResult.getScanRecord().getServiceUuids()) {
                if (uuid.getUuid().toString().equalsIgnoreCase(Uuids.v3.BLUSTREAM_SERVICE.toString())) {
                    return new SensorParseResult(SensorParseResult.SensorMode.V3,
                            scanResult.getDevice().getAddress(),
                            new SerialNumberHelper(mCompatibleDeviceIdentifiers).getSerialFromScanResult(scanResult));
                }
            }
        }
        return null;
    }

    public SensorParseResult parseV4(ScanResult scanResult) {
        if (!isValidScanResult(scanResult)) {
            return null;
        }

        if (scanResult.getScanRecord().getServiceUuids() != null) {
            for (ParcelUuid uuid : scanResult.getScanRecord().getServiceUuids()) {
                if (uuid.getUuid().toString().equalsIgnoreCase(Uuids.v4.BLUSTREAM_SERVICE.toString())) {
                    return new SensorParseResult(SensorParseResult.SensorMode.V4,
                            scanResult.getDevice().getAddress(),
                            new SerialNumberHelper(mCompatibleDeviceIdentifiers).getSerialFromScanResult(scanResult));
                }
            }
        }
        return null;
    }

    private static boolean isValidScanResult(ScanResult scanResult) {
        return scanResult != null && scanResult.getScanRecord() != null;
    }
}
