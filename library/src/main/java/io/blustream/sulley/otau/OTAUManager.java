package io.blustream.sulley.otau;

import androidx.annotation.NonNull;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.blustream.sulley.otau.exceptions.OTAUManagerException;
import io.blustream.sulley.sensor.Sensor;

public interface OTAUManager {

    void upgradeSensor(@NonNull Sensor sensor, File imageFile, @NonNull Listener listener);
    boolean stopUpgrade();
    void setDebugMode(boolean debugMode);
    SensorUpgradeState getForceFailAtState();
    void setForceFailAtState(SensorUpgradeState failAtState);

    interface Listener {
        void onUpgradeStarted();

        void onUpgradePercentComplete(int percentComplete);

        default void onUpgradeStateChange(SensorUpgradeState sensorUpgradeState) { /* NOP */}

        void onUpgradeComplete();

        void onUpgradeError(OTAUManagerException ex);
    }

    enum SensorUpgradeState {
        UNKNOWN("OTAU State Unknown", 0),
        RESUMING_UPGRADE_FOR_BOOT_MODE_SENSOR("Resuming Upgrade from Boot Mode", 1),
        CONNECTING_TO_SENSOR("Connecting to sensor", 2),
        CHECKING_OTAU_VERSION("Checking OTAU Version (app mode)", 3),
        GETTING_FIRMWARE_PROPERTIES("Getting Firmware Properties (app mode)", 4),
        PREPARING_BOOT_MODE("Preparing boot mode", 5),
        WRITING_BOOT_INTO_OTAU_MODE_COMMAND("Sending reboot to boot mode command", 6),
        RECONNECTING_TO_BOOT_MODE_SENSOR("Reconnecting to boot mode sensor", 7),
        CHECKING_BOOT_MODE_OTAU_VERSION("Checking OTAU Version (boot mode)", 8),
        GETTING_BOOT_MODE_FIRMWARE_PROPERTIES("Getting Firmware Properties (boot mode)", 9),
        PREPARING_IMAGE_WRITE("Preparing firmware image", 10),
        WRITING_IMAGE("Writing Firmware Image", 11),
        WRITING_BOOT_INTO_APPLICATION_MODE_COMMAND("Sending reboot to app mode command", 12),
        RECONNECTING_TO_APP_MODE_SENSOR("Reconnecting to app mode sensor", 13),
        VERIFYING_OTAU("Verifying OTAU Success", 14),
        COMPLETE("OTAU Complete", 15);

        private String name;
        private int id;

        private static final Map<Integer, SensorUpgradeState> intToTypeMap = new HashMap<>();
        static {
            for (SensorUpgradeState type : SensorUpgradeState.values()) {
                intToTypeMap.put(type.id, type);
            }
        }
        private static final Map<String, SensorUpgradeState> stringToTypeMap = new HashMap<>();
        static {
            for (SensorUpgradeState type : SensorUpgradeState.values()) {
                stringToTypeMap.put(type.name, type);
            }
        }

        public static SensorUpgradeState fromInt(int i) {
            SensorUpgradeState type = intToTypeMap.get(i);
            if (type == null) {
                return SensorUpgradeState.UNKNOWN;
            }
            return type;
        }

        public static SensorUpgradeState fromString(String name) {
            SensorUpgradeState type = stringToTypeMap.get(name);
            if (type == null) {
                return SensorUpgradeState.UNKNOWN;
            }
            return type;
        }

        SensorUpgradeState(String name, int id) {
            this.id = id;
            this.name = name;
        }

        public int getValue() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getName(int id) {
            for (SensorUpgradeState defs : SensorUpgradeState.values()) {
                if (defs.id == id) {
                    return defs.name;
                }
            }
            return null;
        }

        public int getId(String name) {
            for (SensorUpgradeState def : SensorUpgradeState.values()) {
                if (def.name.equalsIgnoreCase(name)) {
                    return def.id;
                }
            }

            return -1;
        }


    }

}
