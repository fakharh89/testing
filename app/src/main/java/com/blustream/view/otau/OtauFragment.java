package com.blustream.view.otau;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.blustream.demo.R;
import com.blustream.demo.TestDataIntegrityActivity;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.blustream.logger.Log;

import com.blustream.demo.definitions.FirmwareImageDefinitions;

import io.blustream.sulley.otau.OTAUManager;
import io.blustream.sulley.otau.exceptions.OTAUManagerException;
import io.blustream.sulley.otau.OTAUManagerImpl;
import io.blustream.sulley.otau.exceptions.SensorConnectException;
import io.blustream.sulley.otau.helpers.OTAUFirmwareImageHelper;
import io.blustream.sulley.sensor.Sensor;
import io.blustream.sulley.sensor.SensorConnectionState;
import io.blustream.sulley.sensor.SensorLifecycleListener;
import io.blustream.sulley.sensor.SensorManagerImpl;
import io.blustream.sulley.utilities.ImageEditor;

import static com.blustream.demo.TestDataIntegrityActivity.SENSOR_KEY;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OtauFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OtauFragment extends Fragment {

    @BindView(R.id.otau_button)
    protected Button otauButton;
    @BindView(R.id.connectSwitch)
    protected Switch connectSwitch;
    @BindView(R.id.sensorVersionTextView)
    protected TextView sensorVersionTextView;
    @BindView(R.id.otauProgressTextView)
    protected TextView otauProgressTextView;
    @BindView(R.id.firmwareImageSelectorSpinner)
    protected Spinner firmwareImageSelectorSpinner;
    @BindView(R.id.progressBar)
    protected ProgressBar progressBar;
    @BindView(R.id.btn_set_fail_point)
    protected Button btn_set_fail_point;

    private Map<String, Integer> firmwareImageMap = new HashMap<>();
    private Map<String, Integer> foreFailPointMap = new HashMap<>();
    private String selectedImage;
    private MutableLiveData<Boolean> isUpgradeRunning = new MutableLiveData<>();
    private MutableLiveData<Boolean> isSensorConnected = new MutableLiveData<>();
    private Sensor sensor;
    private OTAUManager otauManager;

    public static OtauFragment newInstance(String sensorSerialNumber) {
        OtauFragment fragment = new OtauFragment();
        Bundle args = new Bundle();
        args.putString(SENSOR_KEY, sensorSerialNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private Context getAppContext() {
        return getActivity() == null ? null : getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.otau_fragment, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WeakReference<TestDataIntegrityActivity> parentActivity = new WeakReference<>((TestDataIntegrityActivity) getActivity());
        if (getArguments() != null) {
            String sensorSerial = getArguments().getString(SENSOR_KEY);
            sensor = SensorManagerImpl.getInstance().getSensorCache().getExistingSensorFromSerialNumber(sensorSerial);
            if (sensor != null) {
                sensor.setListener(createLifeCycleListener());
            }
        }
        parentActivity.get().setTitle(sensor.getSerialNumber());
        init();
        // TODO: Use the ViewModel
    }

    @OnCheckedChanged(R.id.connectSwitch)
    void onConnectSwitchChanged(CompoundButton button, boolean checked) {
        Log.d("onConnectSwitchChanged. button = " + button.getText() + " checked = " + checked);
        if (checked) {
            if (sensor.getState() == SensorConnectionState.DISCONNECTED) {
                sensor.connect();
                progressBar.setVisibility(View.VISIBLE);
            }
        } else if (sensor.getState() == SensorConnectionState.CONNECTED) {
            sensor.disconnect();
            progressBar.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.otau_button)
    void onOTAUClicked() {
        if (isUpgradeRunning.getValue() != null && isUpgradeRunning.getValue()) {
            stopUpgrade();
        } else {
            writeSelectedFirmware();
        }
    }

    @OnClick(R.id.btn_set_fail_point)
    void onSetFailPointClicked() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_force_error, null);
        final Spinner forceFailSpinner = (Spinner) dialogView.findViewById(R.id.force_error_spinner);
        if (forceFailSpinner == null) {
            Toast.makeText(getAppContext(), "Failed to create force fail spinner", Toast.LENGTH_LONG).show();
            return;
        }

        alertBuilder.setPositiveButton("Set", (dialogInterface, i) -> {
            otauManager.setDebugMode(true);
            otauManager.setForceFailAtState(OTAUManager.SensorUpgradeState.fromString(forceFailSpinner.getSelectedItem().toString()));
        });

        alertBuilder.setNegativeButton("No Fails", (dialogInterface, i) -> {
            otauManager.setForceFailAtState(null);
            otauManager.setDebugMode(false);
        });

        initForceFailMap();
        initForceFailSpinner(forceFailSpinner);

        alertBuilder.setView(dialogView);
        alertBuilder.create().show();


    }

    private void writeSelectedFirmware() {
        otauButton.setText(getString(R.string.stop_otau));
        FirmwareImageDefinitions.firmwareVersions firmwareImageDefinition = FirmwareImageDefinitions.firmwareVersions.fromString(selectedImage);
        File firmwareImage;

        Context context = getAppContext();

        if (context == null) {
            Log.d("Cannot get firmware image file without a valid context.");
            handleEvent("writeSelectedFirmware Failed! Context was null.");
            return;
        }

        String filename;

        switch (firmwareImageDefinition) {
            case HUMIDITRAK_V1_0_0:
                filename = "humiditrak_1_0_0";
                break;
            case HUMIDITRAK_V3_0_2:
                filename = "humiditrak_3_0_2";
                break;
            case HUMIDITRAK_V3_0_3_1_DOWNGRADE:
                filename = "humiditrak_3_0_3_1_downgrade";
                break;
            case HUMIDITRAK_V4_0_9:
                filename = "humiditrak_4_0_9";
                break;
            case TAYLOR_V3_0_2:
                filename = "taylor_3_0_2";
                break;
            case TAYLOR_V3_0_3:
                filename = "taylor_3_0_3";
                break;
            case TKL_V3_0_2:
                filename = "tkl_3_0_2";
                break;
            case TKL_V3_0_3:
                filename = "tkl_3_0_3";
                break;
            case TKL_V3_0_3_1_DOWNGRADE:
                filename = "tkl_3_0_3_1_downgrade";
                break;
            case TKL_V4_0_6:
                filename = "tkl_4_0_6";
                break;
            case TKL_V4_0_8:
                filename = "tkl_4_0_8";
                break;
            case TKL_V4_0_9:
                filename = "tkl_4_0_9";
                break;
            case BLUSTREAM_V3_0_2:
                filename = "blustream_3_0_2";
                break;
            case BLUSTREAM_V3_0_3:
                filename = "blustream_3_0_3";
                break;
            case BLUSTREAM_V4_0_9:
                filename = "blustream_4_0_9";
                break;
            case BLUSTREAM_V4_0_10:
                filename = "blustream_4_0_10";
                break;
            case TAYLOR_FORCE_BAD_DATA_TEST:
                filename = "gatt_server_update";
                break;
            default:
                filename = null;

        }

        if (filename == null) {
            return;
        }

        firmwareImage = new File(getAppContext().getExternalFilesDir(null), filename + ".img");

        if (!firmwareImage.canRead()) {
            //Isn't  an external file. Check assets raw folder.
            InputStream inputStream = getAppContext().getResources().openRawResource(getOriginalImgId(firmwareImage));
            firmwareImage = OTAUFirmwareImageHelper.cacheStockFirmwareImage(getAppContext(), firmwareImage.getName(), inputStream);
        }
        otauManager.upgradeSensor(sensor, firmwareImage, otauManagerListener);

    }

    private OTAUManager.Listener otauManagerListener = new OTAUManager.Listener() {
        @Override
        public void onUpgradeStarted() {
            handleEvent(String.format(Locale.getDefault(), "Upgrade on sensor: %s has started.", sensor.getSerialNumber()));
            isUpgradeRunning.postValue(true);
        }

        @Override
        public void onUpgradePercentComplete(int percentComplete) {
            handleEvent(String.format(Locale.getDefault(), "Write Firmware Progress: %d%%", percentComplete));
        }

        @Override
        public void onUpgradeStateChange(OTAUManager.SensorUpgradeState sensorUpgradeState) {
            String message = "";
            switch (sensorUpgradeState) {
                case CONNECTING_TO_SENSOR:
                    progressBar.setVisibility(View.VISIBLE);
                    message = String.format(Locale.getDefault(), "Connecting to sensor %s", sensor.getSerialNumber());
                    break;
                case RESUMING_UPGRADE_FOR_BOOT_MODE_SENSOR:
                    message = "Resuming upgrade for boot mode sensor";
                    break;
                case CHECKING_OTAU_VERSION:
                    progressBar.setVisibility(View.GONE);
                    message = "Checking OTAU Version is compatible.";
                    break;
                case CHECKING_BOOT_MODE_OTAU_VERSION:
                    message = "Checking boot mode OTAU Version";
                    break;
                case GETTING_BOOT_MODE_FIRMWARE_PROPERTIES:
                    message = "Getting boot mode firmware properties.";
                    break;
                case GETTING_FIRMWARE_PROPERTIES:
                    message = "Getting firmware properties.";
                    break;
                case PREPARING_IMAGE_WRITE:
                    message = "Initializing firmware image.";
                    break;
                case WRITING_IMAGE:
                    message = "Writing firmware image";
                    break;
                case RECONNECTING_TO_BOOT_MODE_SENSOR:
                    message = "Rebooting sensor to boot mode.";
                    break;
                case VERIFYING_OTAU:
                    message = "Verifying firmware properties.";
                    break;
                case COMPLETE:
                    message = String.format(Locale.getDefault(), "Upgrade of sensor %s complete!", sensor.getSerialNumber());
            }

            handleEvent(message);
        }

        @Override
        public void onUpgradeComplete() {
            sensor.setListener(createLifeCycleListener());
            initObservers();
            handleEvent(String.format(Locale.getDefault(), "Upgrade on sensor: %s has completed.", sensor.getSerialNumber()));
            sensor.disconnect();
            otauButton.setText(getString(R.string.start_otau));
            isUpgradeRunning.postValue(false);
        }

        @Override
        public void onUpgradeError(OTAUManagerException ex) {
            handleEvent(String.format(Locale.getDefault(),
                    "Upgrade on sensor: %1$s has failed. \n %2$s \n %3$s",
                    sensor.getSerialNumber(),
                    ex.getMessage(),
                    ex.getResolutionSuggestion()
            ));
            isUpgradeRunning.postValue(false);
            if (ex instanceof SensorConnectException) {
                progressBar.setVisibility(View.GONE);
            }

        }
    };

    private void stopUpgrade() {
        if (otauManager.stopUpgrade()) {
            isUpgradeRunning.postValue(false);
            handleEvent(String.format(Locale.getDefault(), "Sensor %s upgrade has been stopped", sensor.getSerialNumber()));
        } else {
            handleEvent(String.format(Locale.getDefault(), "OTAUManager.stopUpgrade() failed for Sensor %s", sensor.getSerialNumber()));
        }
    }

    private void init() {
        otauManager = new OTAUManagerImpl(Objects.requireNonNull(getAppContext()));
        initViews();
        initObservers();
        isUpgradeRunning.postValue(false);
        isSensorConnected.postValue(sensor.getState().equals(SensorConnectionState.CONNECTED));
    }

    private void initViews() {
        otauProgressTextView.setTextIsSelectable(true);
        otauProgressTextView.setMovementMethod(new ScrollingMovementMethod());
        otauButton.setEnabled(true);
        ViewTreeObserver vto = otauProgressTextView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> {
            final int scrollAmount = otauProgressTextView.getLayout().getLineTop(otauProgressTextView.getLineCount()) - otauProgressTextView.getHeight();
            otauProgressTextView.scrollTo(0, Math.max(scrollAmount, 0));
        });
        initFirmwareImageMap();
        initSpinner();
    }

    private void initObservers() {
        isUpgradeRunning.observe(getViewLifecycleOwner(), this::updateOTAUButtonUIState);
        isSensorConnected.observe(getViewLifecycleOwner(), aBoolean -> {
            connectSwitch.setChecked(aBoolean);

            if (aBoolean) {
                onSensorConnected();
            }
            else {
                if (sensor.getState() != SensorConnectionState.CONNECTED) {
                    onConnectSwitchChanged(connectSwitch, true);
                }
            }

        });
    }

    private SensorLifecycleListener createLifeCycleListener() {
        return new SensorLifecycleListener() {

            @Override
            public void sensorDidReconnect(Sensor sensor) {
                Log.d("sensorDidReconnect" + sensor.getSerialNumber());
            }

            @Override
            public void sensorDidConnect(Sensor sensor2) {
                sensor = sensor2;
                Log.d("sensorDidConnect" + sensor2.getSerialNumber());
                handleEvent(getString(R.string.sensor_connected));
                isSensorConnected.postValue(true);
//                if (isUpgradeRunning.getValue() != null && isUpgradeRunning.getValue()) {
//                    isUpgradeRunning.postValue(false);
//                }
            }

            @Override
            public void sensorDidDisconnect(Sensor sensor) {
                handleEvent(getString(R.string.sensor_disconnected));
                sensorVersionTextView.setText(getString(R.string.sensor_version, "Not connected"));
                isSensorConnected.postValue(false);
                if (isUpgradeRunning.getValue() != null && isUpgradeRunning.getValue()) {
                    sensor.connect();
                }
            }
        };
    }

    private void onSensorConnected() {
        progressBar.setVisibility(View.GONE);
        sensorVersionTextView.setText(getString(R.string.sensor_version, sensor.getSoftwareVersion()));
    }

    private void initFirmwareImageMap() {
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.BLUSTREAM_V3_0_2.getName(), FirmwareImageDefinitions.firmwareVersions.BLUSTREAM_V3_0_2.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.BLUSTREAM_V3_0_3.getName(), FirmwareImageDefinitions.firmwareVersions.BLUSTREAM_V3_0_3.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.HUMIDITRAK_V3_0_3_1_DOWNGRADE.getName(), FirmwareImageDefinitions.firmwareVersions.HUMIDITRAK_V3_0_3_1_DOWNGRADE.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.BLUSTREAM_V4_0_9.getName(), FirmwareImageDefinitions.firmwareVersions.BLUSTREAM_V4_0_9.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.BLUSTREAM_V4_0_10.getName(), FirmwareImageDefinitions.firmwareVersions.BLUSTREAM_V4_0_10.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.HUMIDITRAK_V1_0_0.getName(), FirmwareImageDefinitions.firmwareVersions.HUMIDITRAK_V1_0_0.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.HUMIDITRAK_V3_0_2.getName(), FirmwareImageDefinitions.firmwareVersions.HUMIDITRAK_V3_0_2.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.HUMIDITRAK_V4_0_9.getName(), FirmwareImageDefinitions.firmwareVersions.HUMIDITRAK_V4_0_9.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.TAYLOR_V3_0_2.getName(), FirmwareImageDefinitions.firmwareVersions.TAYLOR_V3_0_2.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.TAYLOR_V3_0_3.getName(), FirmwareImageDefinitions.firmwareVersions.TAYLOR_V3_0_3.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.TAYLOR_FORCE_BAD_DATA_TEST.getName(), FirmwareImageDefinitions.firmwareVersions.TAYLOR_FORCE_BAD_DATA_TEST.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.TKL_V3_0_2.getName(), FirmwareImageDefinitions.firmwareVersions.TKL_V3_0_2.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.TKL_V3_0_3.getName(), FirmwareImageDefinitions.firmwareVersions.TKL_V3_0_3.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.TKL_V3_0_3_1_DOWNGRADE.getName(), FirmwareImageDefinitions.firmwareVersions.TKL_V3_0_3_1_DOWNGRADE.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.TKL_V4_0_6.getName(), FirmwareImageDefinitions.firmwareVersions.TKL_V4_0_6.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.TKL_V4_0_8.getName(), FirmwareImageDefinitions.firmwareVersions.TKL_V4_0_8.getValue());
        firmwareImageMap.put(FirmwareImageDefinitions.firmwareVersions.TKL_V4_0_9.getName(), FirmwareImageDefinitions.firmwareVersions.TKL_V4_0_9.getValue());

    }

    private void initForceFailMap() {
        foreFailPointMap.put(OTAUManager.SensorUpgradeState.CONNECTING_TO_SENSOR.getName(), OTAUManager.SensorUpgradeState.CONNECTING_TO_SENSOR.getValue());
        foreFailPointMap.put(OTAUManager.SensorUpgradeState.CHECKING_OTAU_VERSION.getName(), OTAUManager.SensorUpgradeState.CHECKING_OTAU_VERSION.getValue());
        foreFailPointMap.put(OTAUManager.SensorUpgradeState.GETTING_FIRMWARE_PROPERTIES.getName(), OTAUManager.SensorUpgradeState.GETTING_FIRMWARE_PROPERTIES.getValue());
        //foreFailPointMap.put(OTAUManager.SensorUpgradeState.PREPARING_BOOT_MODE.getName(), OTAUManager.SensorUpgradeState.PREPARING_BOOT_MODE.getValue());
        foreFailPointMap.put(OTAUManager.SensorUpgradeState.RECONNECTING_TO_BOOT_MODE_SENSOR.getName(), OTAUManager.SensorUpgradeState.RECONNECTING_TO_BOOT_MODE_SENSOR.getValue());
        foreFailPointMap.put(OTAUManager.SensorUpgradeState.PREPARING_IMAGE_WRITE.getName(), OTAUManager.SensorUpgradeState.PREPARING_IMAGE_WRITE.getValue());
        foreFailPointMap.put(OTAUManager.SensorUpgradeState.WRITING_IMAGE.getName(), OTAUManager.SensorUpgradeState.WRITING_IMAGE.getValue());
        foreFailPointMap.put(OTAUManager.SensorUpgradeState.RECONNECTING_TO_APP_MODE_SENSOR.getName(), OTAUManager.SensorUpgradeState.RECONNECTING_TO_APP_MODE_SENSOR.getValue());
        foreFailPointMap.put(OTAUManager.SensorUpgradeState.VERIFYING_OTAU.getName(), OTAUManager.SensorUpgradeState.VERIFYING_OTAU.getValue());


    }

    private void initSpinner() {
        String[] routines = firmwareImageMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_spinner_item, routines);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        firmwareImageSelectorSpinner.setAdapter(dataAdapter);
        firmwareImageSelectorSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedImage = adapterView.getItemAtPosition(i).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }
        );
    }

    private void initForceFailSpinner(Spinner forceFailSpinner) {
        String[] failPoints = foreFailPointMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_spinner_item, failPoints);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        forceFailSpinner.setAdapter(dataAdapter);
    }

    private void updateOTAUButtonUIState(boolean isRunning) {
        if (isRunning) {
            connectSwitch.setEnabled(false);
            otauButton.setText(R.string.stop_otau);
        } else {
            connectSwitch.setEnabled(true);
            otauButton.setText(R.string.start_otau);
        }
    }

    private void handleEvent(String text) {
        Log.d("handleEvent. text " + text);
        otauProgressTextView.append(text + "\n");
    }

    private int getOriginalImgId(File file) {
        String filename = file.getName().substring(0, file.getName().indexOf("."));
        int id = Objects.requireNonNull(getAppContext()).getResources().getIdentifier(filename,
                "raw", getAppContext().getPackageName());
        android.util.Log.d(ImageEditor.TAG, "getOriginalImgId = " + id);
        return id;
    }

}
