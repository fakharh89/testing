package io.blustream.sulley.utilities;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.text.TextUtils;

import java.util.Date;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.BlustreamManufacturerDataMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.BlustreamManufacturerData;
import io.blustream.sulley.sensor.ScanUtils;

public class SerialNumberHelper {
    private final String[] mCompatibleSensorIdentifiers;

    public SerialNumberHelper(String[] compatibleSensorIdentifiers) {
        mCompatibleSensorIdentifiers = compatibleSensorIdentifiers;
    }

    public boolean serialNumberMatchesIdentifierArray(String serialNumber) {
        if (mCompatibleSensorIdentifiers == null) {
            return false;
        }

        if (mCompatibleSensorIdentifiers.length == 0) {
            return false;
        }

        if (serialNumber.length() != 8) {
            return false;
        }

        for (String identifier : mCompatibleSensorIdentifiers) {
            if (serialNumber.endsWith(identifier)) {
                return true;
            }
        }

        return false;
    }
    
    public String getSerialFromMac(String macAddress, String deviceName) {
        if (TextUtils.isEmpty(macAddress) || TextUtils.isEmpty(deviceName)) {
            return null;
        }

        String[] addressSplit = macAddress.split(":");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(addressSplit[3])
                .append(addressSplit[4])
                .append(addressSplit[5]);
        String typeCode = getSensorTypeCodeFromOTAUName(deviceName);
        if (TextUtils.isEmpty(typeCode)) {
            return null;
        }
        stringBuilder.append(typeCode);
        return stringBuilder.toString().toLowerCase();
    }

    private String getSensorTypeCodeFromOTAUName(String otauName) {
        if (TextUtils.isEmpty(otauName)) {
            return null;
        }

        if (otauName.equalsIgnoreCase("DA-OTA")) {
            //Original Blustream name??
            //TODO: Figure out what this name is! return "42" for now.
            return "42";
        } else if(otauName.equalsIgnoreCase("Blustream-OTA")) {
            return "42";
        }else if(otauName.equalsIgnoreCase("Humiditrak") ||
                otauName.equalsIgnoreCase("Humiditrak-OTA") ||
                otauName.equalsIgnoreCase("AS-D'Addario")) {
            return "01";
        }else if(otauName.equalsIgnoreCase("SafeNSound-OTA") ||
                otauName.equalsIgnoreCase("Safe&Sound")) {
            return "02";
        } else if (otauName.equalsIgnoreCase("TaylorSense-OTA")) {
            return "10";
        }

        return null;
    }

    /**
     * Gets the device serial number from scanResult.
     *
     * @param scanResult
     * @return Serial number, or null if cannot be parsed
     */
    public String getSerialFromScanResult(ScanResult scanResult) {
        ScanRecord scanRecord = scanResult.getScanRecord();

        if (scanRecord == null) {
            return null;
        }

        byte[] msdBytes = ScanUtils.getMsdBytes(scanResult);
        BlustreamManufacturerDataMapper mapper = new BlustreamManufacturerDataMapper();
        BlustreamManufacturerData manufacturerData = null;
        try {
            manufacturerData = mapper.fromBytes(new Date(), msdBytes);
        } catch (MapperException e) {
            //Log.d("catch MapperException.");
        }

        if (manufacturerData != null && !TextUtils.isEmpty(manufacturerData.getSerialNumber())) {
            if (isSerialValid(manufacturerData.getSerialNumber())) {
                return manufacturerData.getSerialNumber();
            }
        }
        else {
            String serial = getSerialFromMac(scanResult.getDevice().getAddress(), scanResult.getScanRecord().getDeviceName());
            if (isSerialValid(serial)) {
                return serial;
            }
        }
        return null;
    }

    public boolean isSerialValid(String serial) {
        if (TextUtils.isEmpty(serial) || !serialNumberMatchesIdentifierArray(serial)) {
            Log.d("!serialNumberHelper.serialNumberMatchesIdentifierArray(serialNumber,\n" +
                    "                mCompatibleSensorIdentifiers). returned.");
            return false;
        }
        return true;
    }
}
