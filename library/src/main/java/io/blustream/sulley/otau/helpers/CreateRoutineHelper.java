package io.blustream.sulley.otau.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import io.blustream.logger.Log;

import io.blustream.sulley.otau.database.PsKeyDatabaseLoader;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.routines.BootModeGetOTAUVersionRoutine;
import io.blustream.sulley.routines.BootModeGetOTAUVersionRoutineImpl;
import io.blustream.sulley.routines.GetOTAUVersionRoutine;
import io.blustream.sulley.routines.GetOTAUVersionRoutineImpl;
import io.blustream.sulley.routines.OTAUBleDefinitions;
import io.blustream.sulley.routines.OTAUBootModeBleDefinitions;
import io.blustream.sulley.routines.OTAUBootModePropertiesRoutine;
import io.blustream.sulley.routines.OTAUBootModePropertiesRoutineImpl;
import io.blustream.sulley.routines.OTAUPropertiesRoutine;
import io.blustream.sulley.routines.OTAUPropertiesRoutineImpl;
import io.blustream.sulley.routines.OTAUSetAppModeRoutine;
import io.blustream.sulley.routines.OTAUSetAppModeRoutineImpl;
import io.blustream.sulley.routines.OTAUSetBootModeRoutine;
import io.blustream.sulley.routines.OTAUSetBootModeRoutineImpl;
import io.blustream.sulley.routines.WriteFirmwareImageRoutine;
import io.blustream.sulley.routines.WriteFirmwareImageRoutineImpl;
import io.blustream.sulley.sensor.Sensor;

public class CreateRoutineHelper {
    public static OTAUPropertiesRoutine createOTAUPropertiesRoutine(@NonNull Sensor sensor, @NonNull Context context) {
        PsKeyDatabaseLoader loader = new PsKeyDatabaseLoader(context);
        PsKeyDatabase psKeyDatabase;
        try {
            psKeyDatabase = loader.loadPsKeyDatabase();
        } catch (IOException e) {
            Log.e("Failed to load ps key database!", e);
            return null;
        }

        return new OTAUPropertiesRoutineImpl(sensor, new OTAUBleDefinitions(), psKeyDatabase);
    }

    public static OTAUBootModePropertiesRoutine createOTAUBootModePropertiesRoutine(@NonNull Sensor sensor, @NonNull Context context) {
        PsKeyDatabaseLoader loader = new PsKeyDatabaseLoader(context);
        PsKeyDatabase psKeyDatabase;
        try {
            psKeyDatabase = loader.loadPsKeyDatabase();
        } catch (IOException e) {
            Log.e("Failed to load ps key database!", e);
            return null;
        }

        return new OTAUBootModePropertiesRoutineImpl(sensor, new OTAUBootModeBleDefinitions(), psKeyDatabase);
    }

    public static GetOTAUVersionRoutine createOTAUVersionRoutine(@NonNull Sensor sensor) {
        return new GetOTAUVersionRoutineImpl(sensor, new OTAUBleDefinitions());
    }

    public static BootModeGetOTAUVersionRoutine createBootModeOTAUVersionRoutine(@NonNull Sensor sensor) {
        return new BootModeGetOTAUVersionRoutineImpl(sensor, new OTAUBootModeBleDefinitions());
    }

    public static WriteFirmwareImageRoutine createWriteFirmwareImageRoutine(@NonNull Sensor sensor, @NonNull byte[] imageToWrite, @NonNull WriteFirmwareImageRoutine.Listener listener) {
        return new WriteFirmwareImageRoutineImpl(sensor, imageToWrite, listener, new OTAUBootModeBleDefinitions());
    }

    public static OTAUSetBootModeRoutine createSetBootModeRoutine(@NonNull Sensor sensor, @NonNull OTAUBleDefinitions definitions, OTAUSetBootModeRoutine.Listener listener) {
        OTAUSetBootModeRoutine routine = new OTAUSetBootModeRoutineImpl(sensor, definitions);
        routine.setListener(listener);
        return routine;
    }

    public static OTAUSetAppModeRoutine createSetAppModeRoutine(@NonNull Sensor sensor, @NonNull OTAUBootModeBleDefinitions definitions, OTAUSetAppModeRoutine.Listener listener) {
        OTAUSetAppModeRoutine routine = new OTAUSetAppModeRoutineImpl(sensor, definitions);
        routine.setListener(listener);
        return routine;
    }

}
