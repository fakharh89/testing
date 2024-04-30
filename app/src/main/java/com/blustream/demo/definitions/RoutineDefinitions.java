package com.blustream.demo.definitions;

import java.util.HashMap;
import java.util.Map;

public class RoutineDefinitions {
    public enum Routines {
        UNKNOWN("UnknownRoutine", 0),
        BLUSTREAM_V1_ROUTINE("V1BlustreamRoutine", 1),
        BLUSTREAM_V3_ROUTINE("V3BlustreamRoutine", 3),
        BLUSTREAM_V4_ROUTINE("V4BlustreamRoutine", 4),
        REGISTRATION_ROUTINE("RegistrationRoutine", 50),
        BATTERY_ROUTINE("BatterySubroutine", 51),
        STATUS_ROUTINE("StatusSubroutine", 52),
        OTAU_PROPERTIES_ROUTINE("OTAUPropertiesRoutine", 53),
        OTAU_VERSION_ROUTINE("OTAUVersionRoutine", 54),
        OTAU_SET_BOOT_MODE_ROUTINE("OTAUSetBootModeRoutine", 55),
        OTAU_BOOT_MODE_PROPERTIES_ROUTINE("BootModeOTAUPropertiesRoutine", 100),
        OTAU_BOOT_MODE_VERSION_ROUTINE("BootModeOTAUVersionRoutine", 101);


        private String name;
        private int id;

        private static final Map<Integer, Routines> intToTypeMap = new HashMap<>();
        static {
            for (Routines type : Routines.values()) {
                intToTypeMap.put(type.id, type);
            }
        }
        private static final Map<String, Routines> stringToTypeMap = new HashMap<>();
        static {
            for (Routines type : Routines.values()) {
                stringToTypeMap.put(type.name, type);
            }
        }

        public static Routines fromInt(int i) {
            Routines type = intToTypeMap.get(i);
            if (type == null) {
                return Routines.UNKNOWN;
            }
            return type;
        }

        public static Routines fromString(String name) {
            Routines type = stringToTypeMap.get(name);
            if (type == null) {
                return Routines.UNKNOWN;
            }
            return type;
        }

        Routines(String name, int id) {
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
            for (Routines defs : Routines.values()) {
                if (defs.id == id) {
                    return defs.name;
                }
            }
            return null;
        }

        public int getId(String name) {
            for (Routines def : Routines.values()) {
                if (def.name.equalsIgnoreCase(name)) {
                    return def.id;
                }
            }

            return -1;
        }

    }
}
