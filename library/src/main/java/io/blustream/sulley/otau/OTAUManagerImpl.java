package io.blustream.sulley.otau;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.idevicesinc.sweetblue.BleDeviceState;

import java.io.File;

import java.io.IOException;
import java.util.Locale;

import io.blustream.logger.Log;
import io.blustream.sulley.otau.database.PsKeyDatabaseLoader;
import io.blustream.sulley.otau.exceptions.CreateRoutineFailedException;
import io.blustream.sulley.otau.exceptions.FirmwarePropertiesMismatchException;
import io.blustream.sulley.otau.exceptions.GenerateFirmwareImageException;
import io.blustream.sulley.otau.exceptions.OTAUManagerException;
import io.blustream.sulley.otau.exceptions.OTAUVersionIncompatibleException;
import io.blustream.sulley.otau.exceptions.PSKeyDatabaseException;
import io.blustream.sulley.otau.exceptions.RoutineExecutionFailedException;
import io.blustream.sulley.otau.exceptions.SensorConnectException;
import io.blustream.sulley.otau.helpers.CreateRoutineHelper;
import io.blustream.sulley.otau.helpers.OTAUFirmwareImageHelper;
import io.blustream.sulley.otau.model.FirmwareProperties;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.routines.BootModeGetOTAUVersionRoutine;
import io.blustream.sulley.routines.DefinitionCheckHelper;
import io.blustream.sulley.routines.GetOTAUVersionRoutine;
import io.blustream.sulley.routines.OTAUBleDefinitions;
import io.blustream.sulley.routines.OTAUBootModeBleDefinitions;
import io.blustream.sulley.routines.OTAUBootModePropertiesRoutine;
import io.blustream.sulley.routines.OTAUBootModePropertiesRoutineImpl;
import io.blustream.sulley.routines.OTAUPropertiesRoutine;
import io.blustream.sulley.routines.OTAUSetBootModeRoutine;
import io.blustream.sulley.routines.Routine;
import io.blustream.sulley.routines.WriteFirmwareImageRoutine;
import io.blustream.sulley.sensor.Sensor;
import io.blustream.sulley.sensor.SensorConnectionState;
import io.blustream.sulley.sensor.SensorLifecycleListener;


public class OTAUManagerImpl implements OTAUManager {
    private boolean debugMode;
    private SensorUpgradeState forceFailAtState = null;
    private OTAUManager.SensorUpgradeState sensorUpgradeState = SensorUpgradeState.UNKNOWN;
    private Context mContext;
    private Listener listener;
    private File imageFile;
    private Sensor sensor;
    private FirmwareProperties firmwareProperties;
    private MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
    private byte[] imageToWrite = null;
    private Handler sensorConnectHandler = new Handler();
    private static final int SENSOR_CONNECT_TIMEOUT_MS = 60 * 1000;
    private Runnable sensorConnectTimeoutRunnnable = new Runnable() {
        @Override
        public void run() {
            sensor.disconnect();
            listener.onUpgradeError(new SensorConnectException());
        }
    };
    private Observer<Boolean> isConnectedObserver = aBoolean -> {
        if (aBoolean) {
            sensorConnectHandler.removeCallbacksAndMessages(null);
            switch (sensorUpgradeState) {
                case UNKNOWN:
                    // Falls through
                case CONNECTING_TO_SENSOR:
                    begin();
                    break;
                case RECONNECTING_TO_APP_MODE_SENSOR:
                    verifyFirmwareProperties();
                    break;
                case RECONNECTING_TO_BOOT_MODE_SENSOR:
                    writeFirmwareImage(imageToWrite);
                    break;
            }
        }
    };

    public OTAUManagerImpl(@NonNull Context context) {
        mContext = context;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public SensorUpgradeState getForceFailAtState() {
        return forceFailAtState;
    }

    @Override
    public void setForceFailAtState(SensorUpgradeState failAtState) {
        if (debugMode) {
            forceFailAtState = failAtState;
            return;
        }
        forceFailAtState = null;
    }

    public void setListener(@NonNull Listener listener) {
        this.listener = listener;
    }

    @Override
    public void upgradeSensor(@NonNull Sensor sensor, @NonNull File imageFile, @NonNull Listener listener) {
        this.sensor = sensor;
        this.imageFile = imageFile;
        this.listener = listener;

        sensorUpgradeState = SensorUpgradeState.UNKNOWN;

        sensor.setListener(new SensorLifecycleListener() {

            @Override
            public void sensorDidReconnect(Sensor sensor) {
                Log.d("sensorDidReconnect" + sensor.getSerialNumber());
                if (isConnected != null) {
                    isConnected.postValue(true);
                }
            }

            @Override
            public void sensorDidConnect(Sensor sensor2) {
                Log.d("sensorDidConnect" + sensor2.getSerialNumber());
                if (isConnected != null) {
                    isConnected.postValue(true);
                }
            }

            @Override
            public void sensorDidDisconnect(Sensor sensor) {
                if (sensor == null) {
                    return;
                }

                Routine routine = sensor.getRoutine();
                if (routine == null) {
                    return;
                }

                Log.d(String.format(Locale.getDefault(), "Sensor %1$s did disconnect. Stopping %2$s routine", sensor.getSerialNumber(), routine.getClass().getSimpleName()));
                routine.stop();
                if (isConnected != null) {
                    isConnected.postValue(false);
                }

                if (sensorUpgradeState == SensorUpgradeState.RECONNECTING_TO_APP_MODE_SENSOR) {
                    sensor.connect();
                    sensorConnectHandler.postDelayed(sensorConnectTimeoutRunnnable, SENSOR_CONNECT_TIMEOUT_MS);
                    listener.onUpgradeStateChange(SensorUpgradeState.RECONNECTING_TO_APP_MODE_SENSOR);
                }
            }
        });

        initObservers();

        if (this.sensor.getState() != SensorConnectionState.CONNECTED) {
            Log.d("OTAUManager connecting to sensor" + this.sensor.getSerialNumber());
            sensorUpgradeState = SensorUpgradeState.CONNECTING_TO_SENSOR;
            listener.onUpgradeStateChange(sensorUpgradeState);
            if ((BleDeviceState.BONDED.overlaps(sensor.getBleDevice().getNativeStateMask()))) {
                this.sensor.getBleDevice().unbond();
            }

            if (!debugMode || forceFailAtState != sensorUpgradeState) {
                this.sensor.connect();
            }
            sensorConnectHandler.postDelayed(sensorConnectTimeoutRunnnable, SENSOR_CONNECT_TIMEOUT_MS);
            return;
        }

        begin();
    }

    private synchronized void begin() {
        if (sensorUpgradeState != SensorUpgradeState.UNKNOWN &&
            sensorUpgradeState != SensorUpgradeState.CONNECTING_TO_SENSOR) {
            return;
        }

        if (DefinitionCheckHelper.checkSensor(new OTAUBootModeBleDefinitions(), sensor)) {
            resumeSensorUpgrade();
        } else {
            beginSensorUpdate();
        }
    }

    @Override
    public boolean stopUpgrade() {
        if (listener != null) {
            listener.onUpgradeError(new OTAUManagerException("OTAUManager.stopUpgrade called, no action taken. Not supported at this time"));
        }
        return false;
    }

    private void beginSensorUpdate() {
        checkOTAUVersionCompatible();
    }

    private void resumeSensorUpgrade() {
        sensorUpgradeState = SensorUpgradeState.RESUMING_UPGRADE_FOR_BOOT_MODE_SENSOR;
        listener.onUpgradeStateChange(sensorUpgradeState);
        checkBootModeOTAUVersionCompatible();
    }

    private void checkOTAUVersionCompatible() {
        sensorUpgradeState = SensorUpgradeState.CHECKING_OTAU_VERSION;
        listener.onUpgradeStateChange(sensorUpgradeState);
        GetOTAUVersionRoutine routine = CreateRoutineHelper.createOTAUVersionRoutine(sensor);
        if (!routine.isSensorCompatible(sensor)) {
            onUpgradeError(new CreateRoutineFailedException(GetOTAUVersionRoutine.class.getSimpleName()));
            return;
        }

        routine.setListener(new GetOTAUVersionRoutine.Listener() {
            @Override
            public void onOTAUVersionSuccess(Integer otauVersion) {
                if (otauVersion != 6 || (debugMode && forceFailAtState == sensorUpgradeState)) {
                    onUpgradeError(new OTAUVersionIncompatibleException(otauVersion));
                    return;
                }
                getFirmwareProperties();
            }

            @Override
            public void didEncounterError() {
                onUpgradeError(new RoutineExecutionFailedException(GetOTAUVersionRoutine.class.getSimpleName()));
            }
        });

        runRoutine(routine);
    }

    private void checkBootModeOTAUVersionCompatible() {
        sensorUpgradeState = SensorUpgradeState.CHECKING_BOOT_MODE_OTAU_VERSION;
        listener.onUpgradeStateChange(sensorUpgradeState);
        BootModeGetOTAUVersionRoutine routine = CreateRoutineHelper.createBootModeOTAUVersionRoutine(sensor);
        if (!routine.isSensorCompatible(sensor)) {
            onUpgradeError(new CreateRoutineFailedException(BootModeGetOTAUVersionRoutine.class.getSimpleName()));
            return;
        }

        routine.setListener(new BootModeGetOTAUVersionRoutine.Listener() {
            @Override
            public void onOTAUVersionSuccess(Integer otauVersion) {
                if (otauVersion != 6 || (debugMode && forceFailAtState == sensorUpgradeState)) {
                    onUpgradeError(new OTAUVersionIncompatibleException(otauVersion));
                    return;
                }

                FirmwareProperties cachedProperties = OTAUFirmwareImageHelper.getCachedFirmwareProperties(mContext, sensor.getBleDevice().getMacAddress());
                if (cachedProperties == null) {
                    getBootModeFirmwareProperties();
                } else {
                    firmwareProperties = cachedProperties;
                    initFirmwareImage();
                }
            }

            @Override
            public void didEncounterError() {
                onUpgradeError(new RoutineExecutionFailedException(BootModeGetOTAUVersionRoutine.class.getSimpleName()));
            }
        });

        runRoutine(routine);
    }

    private void getBootModeFirmwareProperties() {
        listener.onUpgradeStateChange(SensorUpgradeState.GETTING_BOOT_MODE_FIRMWARE_PROPERTIES);
        OTAUBootModePropertiesRoutine routine = CreateRoutineHelper.createOTAUBootModePropertiesRoutine(sensor, mContext);
        if (routine == null) {
            onUpgradeError(new PSKeyDatabaseException());
            return;
        } else if (!routine.isSensorCompatible(sensor)) {
            onUpgradeError(new CreateRoutineFailedException(OTAUPropertiesRoutine.class.getSimpleName()));
        }

        routine.setListener(new OTAUBootModePropertiesRoutine.Listener() {
            @Override
            public void onSuccess(FirmwareProperties properties) {
                firmwareProperties = properties;
                initFirmwareImage();
            }

            @Override
            public void didEncounterError() {
                onUpgradeError(new RoutineExecutionFailedException(OTAUBootModePropertiesRoutine.class.getSimpleName()));
            }
        });

        if (debugMode && forceFailAtState == sensorUpgradeState) {
            routine.getListener().didEncounterError();
        } else {
            runRoutine(routine);
        }
    }

    private void getFirmwareProperties() {
        sensorUpgradeState = SensorUpgradeState.GETTING_FIRMWARE_PROPERTIES;
        listener.onUpgradeStateChange(sensorUpgradeState);
        OTAUPropertiesRoutine routine = CreateRoutineHelper.createOTAUPropertiesRoutine(sensor, mContext);
        if (routine == null) {
            onUpgradeError(new PSKeyDatabaseException());
            return;
        } else if (!routine.isSensorCompatible(sensor)) {
            onUpgradeError(new CreateRoutineFailedException(OTAUPropertiesRoutine.class.getSimpleName()));
        }

        routine.setListener(new OTAUPropertiesRoutine.Listener() {
            @Override
            public void onSuccess(FirmwareProperties properties) {
                firmwareProperties = properties;
                OTAUFirmwareImageHelper.cacheFirmwareProperties(mContext, firmwareProperties);
                setBootMode();
            }

            @Override
            public void didEncounterError() {
                onUpgradeError(new RoutineExecutionFailedException(OTAUPropertiesRoutine.class.getSimpleName()));
            }
        });

        if (debugMode && forceFailAtState == sensorUpgradeState) {
            routine.getListener().didEncounterError();
        } else {
            runRoutine(routine);
        }
    }


    private void writeFirmwareImage(byte[] image) {
        sensorUpgradeState = SensorUpgradeState.WRITING_IMAGE;
        listener.onUpgradeStateChange(sensorUpgradeState);
        Log.d("writeFirmwareImage called.");
        Routine routine = CreateRoutineHelper.createWriteFirmwareImageRoutine(sensor, image, new WriteFirmwareImageRoutine.Listener() {
            @Override
            public void onWriteStarted() {
                listener.onUpgradeStarted();
            }

            @Override
            public void writeProgress(int percentComplete) { listener.onUpgradePercentComplete(percentComplete); }

            @Override
            public void onWriteComplete() {
                sensorUpgradeState = SensorUpgradeState.RECONNECTING_TO_APP_MODE_SENSOR;
                listener.onUpgradeStateChange(sensorUpgradeState);
            }

            @Override
            public void didEncounterError() {
                onUpgradeError(new RoutineExecutionFailedException(WriteFirmwareImageRoutine.class.getSimpleName()));
            }
        });

        if (debugMode && forceFailAtState == sensorUpgradeState) {
            ((WriteFirmwareImageRoutine)routine).setSimulateFailure(true);
        }
        runRoutine(routine);
    }

    private void initObservers() {
        new Handler(Looper.getMainLooper()).post( () -> { isConnected.observeForever(isConnectedObserver); });
    }

    private void stopObservers() {
        new Handler(Looper.getMainLooper()).post(() -> { isConnected.removeObserver(isConnectedObserver); });
    }

    private boolean runRoutine(Routine routine) {
        if (routine == null) {
            return false;
        }

        if (!routine.start()) {
            onUpgradeError(new RoutineExecutionFailedException(routine.getClass().getSimpleName()));
            return false;
        }
        return true;
    }

    private void setBootMode() {
        sensorUpgradeState = SensorUpgradeState.RECONNECTING_TO_BOOT_MODE_SENSOR;
        listener.onUpgradeStateChange(sensorUpgradeState);
        Routine routine = CreateRoutineHelper.createSetBootModeRoutine(sensor, new OTAUBleDefinitions(), new OTAUSetBootModeRoutine.Listener() {
            @Override
            public void onSuccess() {
                initFirmwareImage();
            }

            @Override
            public void didEncounterError() {
                onUpgradeError(new RoutineExecutionFailedException(OTAUSetBootModeRoutine.class.getSimpleName()));
            }
        });

        if (debugMode && forceFailAtState == sensorUpgradeState) {
            routine.getListener().didEncounterError();
        } else {
            runRoutine(routine);
        }
    }

    private void verifyFirmwareProperties() {
        sensorUpgradeState = SensorUpgradeState.VERIFYING_OTAU;
        listener.onUpgradeStateChange(sensorUpgradeState);
        OTAUPropertiesRoutine routine = CreateRoutineHelper.createOTAUPropertiesRoutine(sensor, mContext);
        if (routine == null || !routine.isSensorCompatible(sensor)) {
            return;
        }

        routine.setListener(new OTAUPropertiesRoutine.Listener() {
            @Override
            public void onSuccess(FirmwareProperties properties) {
                FirmwareProperties cachedProperties = OTAUFirmwareImageHelper.getCachedFirmwareProperties(mContext, sensor.getNativeDevice().getAddress());
                if (cachedProperties == null) {
                    cachedProperties = firmwareProperties;
                }

                if (debugMode && forceFailAtState == sensorUpgradeState) {
                    listener.onUpgradeError(new FirmwarePropertiesMismatchException());
                } else {
                    if (cachedProperties.getUserKeys() == null) {
                        if (cachedProperties.getMacAddress().equalsIgnoreCase(properties.getMacAddress()) && cachedProperties.getCrystalTrim() == properties.getCrystalTrim()) {
                            listener.onUpgradeComplete();
                        }
                    } else if (cachedProperties.equals(properties)) {
                        listener.onUpgradeComplete();
                    } else {
                        onUpgradeError(new FirmwarePropertiesMismatchException());
                        return;
                    }
                }
                stopObservers();
                sensorUpgradeState = SensorUpgradeState.COMPLETE;
                OTAUFirmwareImageHelper.deleteFirmwarePropertiesCache(mContext, cachedProperties);
            }

            @Override
            public void didEncounterError() {
                onUpgradeError(new RoutineExecutionFailedException(OTAUPropertiesRoutine.class.getSimpleName()));
            }
        });

        runRoutine(routine);

    }

    private void initFirmwareImage() {
        listener.onUpgradeStateChange(SensorUpgradeState.PREPARING_IMAGE_WRITE);
        if (debugMode && forceFailAtState == sensorUpgradeState) {
            listener.onUpgradeError(new GenerateFirmwareImageException());
        } else {
            OTAUFirmwareImageHelper.initFirmwareImage(mContext, firmwareProperties, imageFile, new OTAUFirmwareImageHelper.FirmwareImageHelperListener() {
                @Override
                public void onImageReady(byte[] image) {
                    if (sensorUpgradeState == SensorUpgradeState.RECONNECTING_TO_BOOT_MODE_SENSOR) {
                        imageToWrite = new byte[image.length];
                        System.arraycopy(image, 0, imageToWrite, 0, image.length);
                        if (sensor.getState() != SensorConnectionState.CONNECTED) {
                            sensor.connect();
                            sensorConnectHandler.postDelayed(sensorConnectTimeoutRunnnable, SENSOR_CONNECT_TIMEOUT_MS);
                        }
                        return;
                    }

                    writeFirmwareImage(image);
                }

                @Override
                public void onImageError(OTAUManagerException e) {
                    onUpgradeError(e);
                }
            });
        }
    }

    private void onUpgradeError(OTAUManagerException reason) {
        stopObservers();
        sensor.disconnect();
        if ((BleDeviceState.BONDED.overlaps(sensor.getBleDevice().getNativeStateMask()))) {
            sensor.getBleDevice().unbond();
        }
        sensorUpgradeState = SensorUpgradeState.COMPLETE;
        listener.onUpgradeError(reason);
    }
}
