package io.blustream.sulley.sensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.BlustreamManufacturerDataMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.BlustreamManufacturerData;
import io.blustream.sulley.utilities.MacAddressHelper;
import io.blustream.sulley.utilities.SerialNumberHelper;

import static android.content.Context.BLUETOOTH_SERVICE;

class AlternativeScanner {

    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final BluetoothAdapter mBluetoothAdapter;
    private final BleManager mBleManager;
    String rootStr;
    private BluetoothLeScanner mBluetoothLeScanner;
    private AlternativeScannerListener advListener, proximityListener;
    private SensorCache mSensorCache;
    private boolean isAdvScanning, isProximityScanning, shouldContinueProximity;
    private ScanMode currentMode;
    private Set<String> monitoringSerials;
    private SerialNumberHelper serialNumberHelper;
    private String[] mCompatibleDeviceIdentifiers;

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //Log.d("onScanResult" + result);
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d("onBatchScanResults " + results);
            for (ScanResult result : results) {
                if (ScanUtils.isBlustreamCompatible(result)) {
                    addScanResult(result);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d("onScanFailed " + errorCode);
            String errorString;
            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    errorString = "SCAN_FAILED_ALREADY_STARTED";
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    errorString = "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED";
                    isAdvScanning = false;
                    isProximityScanning = false;
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    errorString = "SCAN_FAILED_INTERNAL_ERROR";
                    isAdvScanning = false;
                    isProximityScanning = false;
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    errorString = "SCAN_FAILED_FEATURE_UNSUPPORTED";
                    isAdvScanning = false;
                    isProximityScanning = false;
                    break;
                default:
                    errorString = "UNKNOWN";
                    break;
            }
            Log.e("BLE Scan Failed: " + errorString);
        }
    };

    public AlternativeScanner(Context context, BleManager bleManager, SensorCache sensorCache, String[] compatibleSensorIdentifiers) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBleManager = bleManager;
        mSensorCache = sensorCache;
        mCompatibleDeviceIdentifiers = compatibleSensorIdentifiers;
        serialNumberHelper = new SerialNumberHelper(mCompatibleDeviceIdentifiers);
    }

    public boolean startProximityMonitoring(AlternativeScannerListener listener, Set<String> monitoringSerials) {
        Log.d("startProximityMonitoring " + monitoringSerials);
        if (mBluetoothAdapter == null || !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return false;
        }
        this.monitoringSerials = monitoringSerials;
        currentMode = ScanMode.PROXIMITY;
        proximityListener = listener;
        shouldContinueProximity = true;
        List<ScanFilter> filters = new ArrayList<>();
        for (String serial : monitoringSerials) {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            String mac = mSensorCache.getExistingSensorFromSerialNumber(serial).getBleDevice().getMacAddress();
            builder.setDeviceAddress(mac);
            filters.add(builder.build());
            Log.d("scanFilter added for " + mac);
        }
        isProximityScanning = true;
        return startNativeScan(filters);
    }

    public void stopProximity() {
        Log.d("stopProximity");
        shouldContinueProximity = false;
        //proximityListener = null;
        stopScan();
    }

    public void stopAdv() {
        Log.d("stopAdv");
        //advListener = null;
        stopScan();
        if (shouldContinueProximity) {
            startProximityMonitoring(proximityListener, monitoringSerials);
        }
    }

    private boolean startNativeScan(List<ScanFilter> filters) {
        Log.d("startNativeScan");
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            return false;
        }

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        return true;
    }

    public boolean startAdv(AlternativeScannerListener listener) {
        Log.d("startAdv");
        if (mBluetoothAdapter == null || !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return false;
        }
        if (isProximityScanning) {
            shouldContinueProximity = true;
            stopScan();
        }
        currentMode = ScanMode.ADV;
        advListener = listener;
        List<ScanFilter> filters = new ArrayList<>();
        isAdvScanning = true;
        return startNativeScan(filters);
    }

    public void stopScan() {
        Log.d("stopScan");
        if (mBluetoothLeScanner != null && (isAdvScanning() || isProximityScanning()) && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            isAdvScanning = false;
            Log.d("stopScan.success");
        }
    }

    public boolean isAdvScanning() {
        Log.d("isAdvScanning " + isAdvScanning);
        return isAdvScanning;
    }

    public boolean isProximityScanning() {
        Log.d("isProximityScanning " + isAdvScanning);
        return isProximityScanning;
    }

    private void handleIBeaconEvent(ScanResult scanResult) {
        String serial = ScanUtils.decodeSerialFromIBeacon(scanResult.getScanRecord().getBytes());
        Sensor sensor = mSensorCache.getExistingSensorFromSerialNumber(serial);
        // sensor was not advertised before.
        if (sensor == null) {
            if (serial != null) {
                sensor = createNewSensor(serial, scanResult.getDevice().getAddress());
            }
        }
        sensor.getVisibilityStatus().setIBeaconAdvertised(true);
        sensor.setAdvertisedRssi(scanResult.getRssi());
        if (currentMode == ScanMode.PROXIMITY && proximityListener != null) {
            proximityListener.onBeaconReceived(sensor, scanResult.getRssi());
        } else if (currentMode == ScanMode.ADV && advListener != null) {
            advListener.onBeaconReceived(sensor, scanResult.getRssi());
        }
    }

    private void handleOTAUSensor(ScanResult scanResult) {
        String serial = serialNumberHelper.getSerialFromScanResult(scanResult);
        if (TextUtils.isEmpty(serial)) {
            Log.d("Cannot handle otau sensor. Serial number could not be parsed.");
            return;
        }

        Sensor sensor = mSensorCache.getExistingSensorFromSerialNumber(serial);
        // sensor was not advertised before.
        if (sensor == null) {
            sensor = createNewSensor(serial, scanResult.getDevice().getAddress());
            mSensorCache.addSensor(sensor);
        }

        sensor.getVisibilityStatus().setOTAUBootModeAdvertised(true);
        sensor.setAdvertisedRssi(scanResult.getRssi());
        if (currentMode == ScanMode.PROXIMITY && proximityListener != null) {
            proximityListener.onBeaconReceived(sensor, scanResult.getRssi());
        } else if (currentMode == ScanMode.ADV && advListener != null) {
            advListener.onBeaconReceived(sensor, scanResult.getRssi());
        }
    }

    private void handleAdvEvent(ScanResult scanResult, BlustreamManufacturerData manufacturerData) {
        String serialNumber = manufacturerData.getSerialNumber();
        if (!serialNumberHelper.isSerialValid(serialNumber)) {
            return;
        }
        Sensor sensor = mSensorCache.getExistingSensorFromSerialNumber(serialNumber);
        if (sensor == null) {
            // sensor was not advertised before.
            if (currentMode == ScanMode.ADV) {
                sensor = createNewSensor(serialNumber, scanResult.getDevice().getAddress());
                mSensorCache.addSensor(sensor);
            } else if (currentMode == ScanMode.PROXIMITY) {
                // do nothing with new Sensors in proximity mode. Could be changed.
                return;
            }
        } else {
            //sensor was advertised before.
            Log.i(sensor.getSerialNumber() + " Advertised in alternative scanner ("
                    + scanResult.getRssi() + ")");
            Log.i(sensor.getSerialNumber() + " Results (Advertisement): "
                    + manufacturerData.getHumidTempSample());
            sensor.getVisibilityStatus().setBlustreamAdvertised(true);
            sensor.setAdvertisedRssi(scanResult.getRssi());

            if (currentMode == ScanMode.ADV && advListener != null) {
                advListener.sensorDidAdvertise(sensor, manufacturerData, scanResult.getRssi());
            }
            if (currentMode == ScanMode.PROXIMITY && proximityListener != null) {
                proximityListener.sensorDidAdvertise(sensor, manufacturerData, scanResult.getRssi());
            }
        }
    }

    private void addScanResult(ScanResult scanResult) {
        if (!ScanUtils.isBlustreamCompatible(scanResult)) {
            return;
        }

        Log.d("addScanResult " + scanResult);

        SensorParseResult parseResult = new SensorParser(mCompatibleDeviceIdentifiers).parse(scanResult);
        if (parseResult == null) {
            Log.d("Failed to parse a Blustream compatible sensor.");
            return;
        }

        switch (parseResult.getSensorMode()) {
            case OTAU:
                handleOTAUSensor(scanResult);
                break;
            case IBEACON:
                handleIBeaconEvent(scanResult);
                break;
            case V1:
            case V3:
            case V4:
                byte[] msdBytes = ScanUtils.getMsdBytes(scanResult);
                if (msdBytes == null) {
                    return;
                }
                try {
                    BlustreamManufacturerDataMapper mapper = new BlustreamManufacturerDataMapper();
                    BlustreamManufacturerData manufacturerData = mapper.fromBytes(new Date(), msdBytes);
                    handleAdvEvent(scanResult, manufacturerData);
                } catch (MapperException exception) {
                    Log.d("catch MapperException.");
                }
        }

    }

    private Sensor createNewSensor(String serial, String mac) {
        BleDevice device = mBleManager.newDevice(mac);
        device.setName(serial);
        return new SensorImpl(serial, device);
    }
}

