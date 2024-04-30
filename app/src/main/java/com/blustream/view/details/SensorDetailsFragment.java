package com.blustream.view.details;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.blustream.demo.R;
import com.blustream.demo.TestDataIntegrityActivity;
import com.blustream.view.base.BaseViewModel;
import com.blustream.view.base.BaseViewModelFactory;
import com.blustream.view.otau.OtauFragment;
import com.blustream.view.proximity.ProximityFragment;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.blustream.logger.Log;
import io.blustream.sulley.models.BatterySample;
import io.blustream.sulley.models.HumidTempSample;
import io.blustream.sulley.models.ImpactSample;
import io.blustream.sulley.models.MotionSample;
import io.blustream.sulley.models.Status;
import io.blustream.sulley.otau.database.PsKeyDatabaseLoader;
import io.blustream.sulley.otau.model.FirmwareProperties;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.repository.data.ProximityLogEvent;
import io.blustream.sulley.routines.BatterySubroutine;
import io.blustream.sulley.routines.BatterySubroutineImpl;
import io.blustream.sulley.routines.BlustreamRoutine;
import io.blustream.sulley.routines.BootModeGetOTAUVersionRoutine;
import io.blustream.sulley.routines.BootModeGetOTAUVersionRoutineImpl;
import io.blustream.sulley.routines.OTAUBootModeBleDefinitions;
import io.blustream.sulley.routines.OTAUBootModePropertiesRoutine;
import io.blustream.sulley.routines.OTAUBootModePropertiesRoutineImpl;
import io.blustream.sulley.routines.OTAUPropertiesRoutine;
import io.blustream.sulley.routines.OTAUPropertiesRoutineImpl;
import io.blustream.sulley.routines.OTAUBleDefinitions;
import io.blustream.sulley.routines.OTAUSetBootModeRoutine;
import io.blustream.sulley.routines.OTAUSetBootModeRoutineImpl;
import io.blustream.sulley.routines.RegistrationRoutine;
import io.blustream.sulley.routines.Routine;
import io.blustream.sulley.routines.RoutineConfig;
import com.blustream.demo.definitions.RoutineDefinitions;
import com.idevicesinc.sweetblue.BleDeviceState;

import io.blustream.sulley.routines.StatusSubroutine;
import io.blustream.sulley.routines.StatusSubroutineImpl;
import io.blustream.sulley.routines.V1Routine;
import io.blustream.sulley.routines.V1RoutineImpl;
import io.blustream.sulley.routines.V3BleDefinitions;
import io.blustream.sulley.routines.V3RegistrationRoutineImpl;
import io.blustream.sulley.routines.V3RoutineImpl;
import io.blustream.sulley.routines.V4BleDefinitions;
import io.blustream.sulley.routines.V4RegistrationRoutineImpl;
import io.blustream.sulley.routines.V4RoutineImpl;
import io.blustream.sulley.routines.WriteFirmwareImageRoutine;
import io.blustream.sulley.routines.WriteFirmwareImageRoutineImpl;
import io.blustream.sulley.sensor.Sensor;
import io.blustream.sulley.sensor.SensorConnectionState;
import io.blustream.sulley.sensor.SensorLifecycleListener;
import io.blustream.sulley.sensor.SensorManagerImpl;

import static com.blustream.demo.TestDataIntegrityActivity.SENSOR_KEY;

public class SensorDetailsFragment extends Fragment {

    @BindView(R.id.startStopButton)
    protected Button startStopButton;
    @BindView(R.id.proximityButton)
    protected Button proximityButton;
    @BindView(R.id.otau_button)
    protected Button otauButton;
    @BindView(R.id.connectSwitch)
    protected Switch connectSwitch;
    @BindView(R.id.sensorVersionTextView)
    protected TextView sensorVersionTextView;
    @BindView(R.id.logLabelTextViewTv)
    protected TextView logLabelTextView;
    @BindView(R.id.logTextView)
    protected TextView logTextView;
    @BindView(R.id.routineSelectorSpinner)
    protected Spinner routineSelectorSpinner;
    @BindView(R.id.progressBar)
    protected ProgressBar progressBar;
    private BaseViewModel mViewModel;

    private Map<String, Integer> routinesMap = new HashMap<>();
    private String selectedRoutine;
    private MutableLiveData<Boolean> isRoutineRunning = new MutableLiveData<>();
    private MutableLiveData<Boolean> isSensorConnected = new MutableLiveData<>();
    private Sensor sensor;
    private WeakReference<TestDataIntegrityActivity> parentActivity;
    private FirmwareProperties firmwareProperties;
    private int writeFirmwarePercentComplete;
    private Routine currentRoutine = null;

    public static SensorDetailsFragment newInstance(String sensorSerial) {
        SensorDetailsFragment fragment = new SensorDetailsFragment();
        Bundle args = new Bundle();
        args.putString(SENSOR_KEY, sensorSerial);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sensor_details_fragment, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parentActivity = new WeakReference<>((TestDataIntegrityActivity) getActivity());
        mViewModel = new ViewModelProvider(this, new BaseViewModelFactory(getActivity().getApplication())).get(BaseViewModel.class);
        if (getArguments() != null) {
            String sensorSerial = getArguments().getString(SENSOR_KEY);
            sensor = SensorManagerImpl.getInstance().getSensorCache().getExistingSensorFromSerialNumber(sensorSerial);
            if (sensor != null) {
                sensor.setListener(createLifeCycleListener());
            }
        }
        if (parentActivity != null) {
            parentActivity.get().setTitle(sensor.getSerialNumber());
        }
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

    @OnClick(R.id.startStopButton)
    void onStartStopClicked() {
        if (isRoutineRunning.getValue() != null && isRoutineRunning.getValue()) {
            stopCurrentRoutine();
        } else {
            startSelectedRoutine();
        }
    }

    @OnClick(R.id.proximityButton)
    void onProximityClicked() {
        ProximityFragment proximityFragment = ProximityFragment.newInstance(sensor.getSerialNumber());
        ((TestDataIntegrityActivity) getActivity()).loadFragment(proximityFragment);
    }

    @OnClick(R.id.otau_button)
    void onOTAUClicked() {
        ((TestDataIntegrityActivity)getActivity()).loadFragment(OtauFragment.newInstance(sensor.getSerialNumber()));
    }

    private void startSelectedRoutine() {
        RoutineDefinitions.Routines routineDefinition = RoutineDefinitions.Routines.fromString(selectedRoutine);
        Routine routine;

        switch (routineDefinition) {
            case BLUSTREAM_V1_ROUTINE:
                routine = createV1Routine();
                break;
            case BLUSTREAM_V3_ROUTINE:
                routine = createV3BlustreamRoutine();
                break;
            case BLUSTREAM_V4_ROUTINE:
                routine = createV4BlustreamRoutine();
                break;
            case BATTERY_ROUTINE:
                routine = createBatterySubroutine();
                break;
            case STATUS_ROUTINE:
                routine = createStatusSubroutine();
                break;
            case REGISTRATION_ROUTINE:
                routine = createRegRoutine();
                break;
            case OTAU_PROPERTIES_ROUTINE:
                routine = createOTAUPropertiesRoutine();
                break;
            case OTAU_VERSION_ROUTINE:
                routine = createOTAUVersionRoutine();
                break;
            case OTAU_SET_BOOT_MODE_ROUTINE:
                routine = createOTAUSetBootModeRoutine();
                final Routine finalRoutine = routine;
                userConfirmedToSetBootMode(() -> {
                    if (isRoutineRunning.getValue() != null && isRoutineRunning.getValue()) {
                        stopCurrentRoutine();
                    }


                    boolean started = finalRoutine.start();
                    if (started) {
                        currentRoutine = finalRoutine;
                    }
                    isRoutineRunning.postValue(started);
                    handleEvent(finalRoutine.toString() + " has been started = " + started);


                });
                return;
            case OTAU_BOOT_MODE_VERSION_ROUTINE:
                routine = createBootModeOtauVersionRoutine();
                break;
            case OTAU_BOOT_MODE_PROPERTIES_ROUTINE:
                routine = createBootModeOTAUPropertiesRoutine();
                break;
            default:
                routine = null;

        }

        if (routine == null) {
            return;
        }


        boolean started = routine.start();
        if (started) {
            currentRoutine = routine;
        }
        isRoutineRunning.postValue(started);
        handleEvent(routine.toString() + " has been started = " + started);

    }

    private OTAUSetBootModeRoutine createOTAUSetBootModeRoutine() {
        OTAUSetBootModeRoutine routine = new OTAUSetBootModeRoutineImpl(sensor, new OTAUBleDefinitions());
        final OTAUSetBootModeRoutine.Listener listener =  new OTAUSetBootModeRoutine.Listener() {
            @Override
            public void onSuccess() {
                handleEvent(String.format(Locale.getDefault(), "%s succeeded!", routine.toString()));
            }

            @Override
            public void didEncounterError() {
                Log.d("didEncounterError");
            }
        };

        routine.setListener(listener);
        return routine;
    }

    private interface SetBootModeUserConfirmationListener {
        void onUserConfirmed();
    }

    private void userConfirmedToSetBootMode(SetBootModeUserConfirmationListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( getContext());
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (listener != null) {
                    listener.onUserConfirmed();
                }
            }
        });

        builder.setNegativeButton("NO!", (dialogInterface, i) -> Log.i("setting boot mode aborted by user."));

        builder.setTitle("WARNING!");
        builder.setMessage("By setting Boot Mode, you will effectively BRICK this sensor until it's either reset in a jig, or an image is written." +
                "Are you SURE you want to proceed??");

        builder.create().show();
    }

    private BootModeGetOTAUVersionRoutine createOTAUVersionRoutine() {
        return new BootModeGetOTAUVersionRoutineImpl(sensor, new OTAUBootModeBleDefinitions());
    }

    private void stopCurrentRoutine() {
        if (currentRoutine != null) {
            currentRoutine.stop();
            isRoutineRunning.postValue(false);
            handleEvent(currentRoutine.toString() + " has been stopped", true);
            currentRoutine = null;
        }
    }

    private void init() {
        initViews();
        initObservers();
        isRoutineRunning.postValue(false);
        isSensorConnected.postValue(sensor.getState().equals(SensorConnectionState.CONNECTED));
        otauButton.setEnabled(true);
    }

    private void initViews() {
        logTextView.setTextIsSelectable(true);
        logTextView.setMovementMethod(new ScrollingMovementMethod());
        ViewTreeObserver vto = logTextView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> {
            final int scrollAmount = logTextView.getLayout().getLineTop(logTextView.getLineCount()) - logTextView.getHeight();
            if (scrollAmount > 0)
                logTextView.scrollTo(0, scrollAmount);
            else
                logTextView.scrollTo(0, 0);
        });
    }

    private void initObservers() {
        isRoutineRunning.observe(getViewLifecycleOwner(), this::updateStartStopButtonUIState);
        isSensorConnected.observe(getViewLifecycleOwner(), aBoolean -> {
            connectSwitch.setChecked(aBoolean);
            startStopButton.setEnabled(aBoolean);

            if (aBoolean) {
                onSensorConnected();
            }

            if (isRoutineRunning.getValue() != null && isRoutineRunning.getValue()) {
                isRoutineRunning.postValue(false);
            }
        });
    }

    private BlustreamRoutine createV3BlustreamRoutine() {
        BlustreamRoutine<BlustreamRoutine.Listener, BlustreamRoutine.Settings> routine;
        routine = new V3RoutineImpl(sensor);
        routine.setListener(createBlustreamRoutineListener());
        return routine;
    }

    private BlustreamRoutine createV4BlustreamRoutine() {
        BlustreamRoutine<BlustreamRoutine.Listener, BlustreamRoutine.Settings> routine;
        routine = new V4RoutineImpl(sensor, new RoutineConfig()); //todo check routine config.
        routine.setListener(createBlustreamRoutineListener());
        return routine;
    }

    private V1Routine createV1Routine() {
        V1Routine routine = new V1RoutineImpl(sensor);
        routine.setListener(createV1RoutineListener());
        return routine;
    }

    private void addLog(String serial, ProximityLogEvent.Type type) {
        Log.d("addLog " + serial);
        ProximityLogEvent event = new ProximityLogEvent(serial, new Date(), type);
        mViewModel.getRepository().addLog(event);
    }

    private SensorLifecycleListener createLifeCycleListener() {
        return new SensorLifecycleListener() {

            @Override
            public void sensorDidReconnect(Sensor sensor) {
                Log.d("sensorDidReconnect" + sensor.getSerialNumber());
                addLog(sensor.getSerialNumber(), ProximityLogEvent.Type.CONNECTION);
            }

            @Override
            public void sensorDidConnect(Sensor sensor2) {
                sensor = sensor2;
                Log.d("sensorDidConnect" + sensor2.getSerialNumber());
                addLog(sensor.getSerialNumber(), ProximityLogEvent.Type.CONNECTION);
                handleEvent(getString(R.string.sensor_connected));
                isSensorConnected.postValue(true);
            }

            @Override
            public void sensorDidDisconnect(Sensor sensor) {
                handleEvent(getString(R.string.sensor_disconnected));
                sensorVersionTextView.setText(getString(R.string.sensor_version, "Not connected"));
                isSensorConnected.postValue(false);
                stopCurrentRoutine();
            }
        };
    }

    private void onSensorConnected() {
        progressBar.setVisibility(View.GONE);
        sensorVersionTextView.setText(getString(R.string.sensor_version, sensor.getSoftwareVersion()));
        initRoutineMap();
        initSpinner();
    }

    private void initRoutineMap() {
        routinesMap.put(RoutineDefinitions.Routines.BLUSTREAM_V1_ROUTINE.getName(), RoutineDefinitions.Routines.BLUSTREAM_V1_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.BLUSTREAM_V3_ROUTINE.getName(), RoutineDefinitions.Routines.BLUSTREAM_V3_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.BLUSTREAM_V4_ROUTINE.getName(), RoutineDefinitions.Routines.BLUSTREAM_V3_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.REGISTRATION_ROUTINE.getName(), RoutineDefinitions.Routines.REGISTRATION_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.BATTERY_ROUTINE.getName(), RoutineDefinitions.Routines.BATTERY_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.STATUS_ROUTINE.getName(), RoutineDefinitions.Routines.STATUS_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.OTAU_PROPERTIES_ROUTINE.getName(), RoutineDefinitions.Routines.OTAU_PROPERTIES_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.OTAU_VERSION_ROUTINE.getName(), RoutineDefinitions.Routines.OTAU_VERSION_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.OTAU_SET_BOOT_MODE_ROUTINE.getName(), RoutineDefinitions.Routines.OTAU_SET_BOOT_MODE_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.OTAU_BOOT_MODE_PROPERTIES_ROUTINE.getName(), RoutineDefinitions.Routines.OTAU_BOOT_MODE_PROPERTIES_ROUTINE.getValue());
        routinesMap.put(RoutineDefinitions.Routines.OTAU_BOOT_MODE_VERSION_ROUTINE.getName(), RoutineDefinitions.Routines.OTAU_BOOT_MODE_VERSION_ROUTINE.getValue());

    }

    private WriteFirmwareImageRoutine.Listener writeFirmwareListener = new WriteFirmwareImageRoutine.Listener() {
        @Override
        public void onWriteStarted() {
            handleEvent("Firmwaew update started");
        }

        @Override
        public void writeProgress(int percentComplete) {
            if (writeFirmwarePercentComplete != percentComplete) {
                writeFirmwarePercentComplete = percentComplete;
                handleEvent(String.format(Locale.getDefault(), "Percent complete: %d%%", percentComplete));


            }
        }

        @Override
        public void onWriteComplete() {
            handleEvent("Firmware update complete! rebooting sensor.");
            boolean didUnbond = sensor.getBleDevice().unbond();
            sensor.disconnect();
        }

        @Override
        public void didEncounterError() {
            handleEvent("Firmware update FAILED!");
        }
    };

    private WriteFirmwareImageRoutine createWriteFirmwareImageRoutine(byte[] imageToWrite, WriteFirmwareImageRoutine.Listener listener) {

        return new WriteFirmwareImageRoutineImpl(sensor, imageToWrite, listener, new OTAUBootModeBleDefinitions());

    }

    private BootModeGetOTAUVersionRoutine createBootModeOtauVersionRoutine() {
        BootModeGetOTAUVersionRoutine routine = new BootModeGetOTAUVersionRoutineImpl(sensor, new OTAUBootModeBleDefinitions());

        routine.setListener(new BootModeGetOTAUVersionRoutine.Listener() {
            @Override
            public void onOTAUVersionSuccess(Integer otauVersion) {
                handleEvent(String.format(Locale.getDefault(), "OTAU Version = %d", otauVersion));

            }

            @Override
            public void didEncounterError() {
                handleEvent("Encountered an error getting OTAU Version");
            }
        });

        return routine;
    }

    private OTAUBootModePropertiesRoutine createBootModeOTAUPropertiesRoutine() {
        PsKeyDatabaseLoader loader = new PsKeyDatabaseLoader(getContext());
        PsKeyDatabase psKeyDatabase;
        try {
            psKeyDatabase = loader.loadPsKeyDatabase();
        } catch (IOException e) {
            Log.e("Failed to load ps key database!", e);
            return null;
        }

        OTAUBootModePropertiesRoutine routine;
        routine = new OTAUBootModePropertiesRoutineImpl(sensor, new OTAUBootModeBleDefinitions(), psKeyDatabase);
        routine.setListener(new OTAUBootModePropertiesRoutineImpl.Listener() {
            @Override
            public void onSuccess(FirmwareProperties properties) {
                if (firmwareProperties == null) {
                    firmwareProperties = new FirmwareProperties();
                }
                firmwareProperties.setMacAddress(properties.getMacAddress());
                firmwareProperties.setUserKeys(properties.getUserKeys());
                firmwareProperties.setCrystalTrim(properties.getCrystalTrim());
                handleEvent(String.format(Locale.getDefault(), "MAC Address: %s", firmwareProperties.getMacAddress()));
                handleEvent(String.format(Locale.getDefault(), "Crystal Trim: %d", firmwareProperties.getCrystalTrim()));
                handleEvent(String.format(Locale.getDefault(), "User Keys: %s", firmwareProperties.getUserKeys()));
            }

            @Override
            public void didEncounterError() {
                Log.d("didEncounterError");
            }
        });
        return routine;

    }

    private OTAUPropertiesRoutine createOTAUPropertiesRoutine() {
        PsKeyDatabaseLoader loader = new PsKeyDatabaseLoader(getContext());
        PsKeyDatabase psKeyDatabase;
        try {
            psKeyDatabase = loader.loadPsKeyDatabase();
        } catch (IOException e) {
            Log.e("Failed to load ps key database!", e);
            return null;
        }

        OTAUPropertiesRoutine routine;
        if (sensor.getVisibilityStatus().isOTAUBootModeAdvertised()) {
            routine = new OTAUPropertiesRoutineImpl(sensor, new OTAUBleDefinitions(), psKeyDatabase);
        } else {
            routine = new OTAUPropertiesRoutineImpl(sensor, new OTAUBleDefinitions(), psKeyDatabase);
        }
        routine.setListener(new OTAUPropertiesRoutineImpl.Listener() {
            @Override
            public void onSuccess(FirmwareProperties properties) {
                if (firmwareProperties == null) {
                    firmwareProperties = new FirmwareProperties();
                }
                firmwareProperties.setMacAddress(properties.getMacAddress());
                firmwareProperties.setUserKeys(properties.getUserKeys());
                firmwareProperties.setCrystalTrim(properties.getCrystalTrim());
                handleEvent(String.format(Locale.getDefault(), "MAC Address: %s", firmwareProperties.getMacAddress()));
                handleEvent(String.format(Locale.getDefault(), "Crystal Trim: %d", firmwareProperties.getCrystalTrim()));
                handleEvent(String.format(Locale.getDefault(), "User Keys: %s", firmwareProperties.getUserKeys()));
            }

            @Override
            public void didEncounterError() {
                Log.d("didEncounterError");
            }
        });
        return routine;
    }

    private StatusSubroutine createStatusSubroutine() {
        StatusSubroutine routine = new StatusSubroutineImpl(sensor, isV4Version() ? new V4BleDefinitions() : new V3BleDefinitions());
        routine.setListener(new StatusSubroutine.Listener() {
            @Override
            public void didGetStatus(Status status) {
                handleEvent("didGetStatus " + status);
            }

            @Override
            public void didEncounterError() {
                handleEvent("didEncounterError");
            }
        });
        return routine;
    }

    private BatterySubroutine createBatterySubroutine() {
        BatterySubroutineImpl routine = new BatterySubroutineImpl(sensor, isV4Version() ? new V4BleDefinitions() : new V3BleDefinitions());
        routine.setListener(new BatterySubroutine.Listener() {
            @Override
            public void didGetBatterySample(BatterySample batterySample) {
                handleEvent("didGetBatterySample " + batterySample);
            }

            @Override
            public void didEncounterError() {
                handleEvent("didEncounterError ");
            }
        });
        return routine;
    }

    private RegistrationRoutine createRegRoutine() {
        RegistrationRoutine registrationRoutine;
        if (isV4Version()) {
            registrationRoutine = new V4RegistrationRoutineImpl(sensor);
        } else {
            registrationRoutine = new V3RegistrationRoutineImpl(sensor);
        }
        registrationRoutine.setListener(createRegistrationRoutineListener());
        return registrationRoutine;
    }

    private RegistrationRoutine.Listener createRegistrationRoutineListener() {
        return new RegistrationRoutine.Listener() {
            @Override
            public void onRegistrationSuccess() {
                handleEvent("Registration Success", true);
            }

            @Override
            public void onRegistrationFailure() {
                handleEvent("Registration Failure", true);
            }

            @Override
            public void didEncounterError() {
                handleEvent("Did Encounter Error", true);
            }
        };
    }

    private void initSpinner() {
        String[] routines = routinesMap.keySet().toArray(new String[routinesMap.keySet().size()]);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, routines);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routineSelectorSpinner.setAdapter(dataAdapter);
        routineSelectorSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedRoutine = adapterView.getItemAtPosition(i).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }
        );
    }

    private BlustreamRoutine.Listener createBlustreamRoutineListener() {
        return new BlustreamRoutine.Listener() {
            @Override
            public void didGetHumidTempSamples(List<HumidTempSample> humidTempSamples) {
                handleEvent("didGetHumidTempSamples " + humidTempSamples);
            }

            @Override
            public void didGetImpactSamples(List<ImpactSample> impactSamples) {
                handleEvent("didGetImpactSamples " + impactSamples);
            }

            @Override
            public void didGetMotionSamples(List<MotionSample> motionSamples) {
                handleEvent("didGetMotionSamples " + motionSamples);
            }

            @Override
            public void didGetStatus(Status status) {
                handleEvent("didGetStatus" + status);
            }

            @Override
            public void didGetBatterySample(BatterySample batterySample) {
                handleEvent("didGetBatterySample " + batterySample);
            }

            @Override
            public void didClearImpactBuffer() {
                handleEvent("didClearImpactBuffer");
            }

            @Override
            public void didClearHumidTempBuffer() {
                handleEvent("didClearHumiTempBuffer");
            }

            @Override
            public void didClearMotionBuffer() {
                handleEvent("didClearMotionBuffer");
            }

            @Override
            public void didConfirmEditableSettings(Sensor sensor) {
                handleEvent("didConfirmEditableSettings");
            }

            @Override
            public void didFailConfirmEditableSettings(Sensor sensor) {
                handleEvent("didFailConfirmEditableSettings");
            }

            @Override
            public void didEncounterError() {
                handleEvent("didEncounterError");
            }
        };
    }

    private V1Routine.Listener createV1RoutineListener() {
        return new V1Routine.Listener() {
            @Override
            public void didGetHumidTempSamples(List<HumidTempSample> humidTempSamples) {
                handleEvent("didGetHumidTempSamples " + humidTempSamples);
            }

            @Override
            public void didGetImpactSamples(List<ImpactSample> impactSamples) {
                handleEvent("didGetImpactSamples " + impactSamples);
            }

            @Override
            public void didGetMotionSamples(List<MotionSample> motionSamples) {
                handleEvent("didGetMotionSamples " + motionSamples);
            }

            @Override
            public void didGetStatus(Status status) {
                handleEvent("didGetStatus " + status);
            }

            @Override
            public void didGetBatterySample(BatterySample batterySample) {
                handleEvent("didGetBatterySample " + batterySample);
            }

            @Override
            public void didConfirmEditableSettings(Sensor sensor) {
                handleEvent("didConfirmEditableSettings");
            }

            @Override
            public void didFailConfirmEditableSettings(Sensor sensor) {
                handleEvent("didFailConfirmEditableSettings");
            }

            @Override
            public void didEncounterError() {
                handleEvent("didEncounterError");
            }
        };
    }

    private void updateStartStopButtonUIState(boolean isRunning) {
        if (isRunning) {
            startStopButton.setText(R.string.stop_ble);
        } else {
            startStopButton.setText(R.string.start_ble);
        }
    }

    private void handleEvent(String text) {
        handleEvent(text, false);
    }

    private void handleEvent(String text, boolean routineStopped) {
        Log.d("handleEvent. text " + text);
        logTextView.append(text + "\n");
        if (routineStopped) {
            isRoutineRunning.postValue(false);
        }
        addLog(sensor.getSerialNumber(), ProximityLogEvent.Type.DATA_RECEIVED);
        sensor.getVisibilityStatus().setConnected(true);
    }

    private boolean isV4Version() {
        return sensor.getSoftwareVersion().startsWith("4");
    }
}
