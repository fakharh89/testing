package com.blustream.demo.definitions;

import java.util.HashMap;
import java.util.Map;

public class FirmwareImageDefinitions {

    public enum firmwareVersions {
        UNKNOWN("UnknownFirmwareImage", 0),
        HUMIDITRAK_V1_0_0("Humiditrak_V1.0.0", 1),
        HUMIDITRAK_V3_0_2("Humiditrak_V3.0.2", 2),
        HUMIDITRAK_V3_0_3_1_DOWNGRADE("humiditrak_3.0.3.1_downgrade", 3),
        HUMIDITRAK_V4_0_9("Humiditrak_V4.0.9", 4),
        TAYLOR_V3_0_2("Taylor_V3.0.2", 50),
        TAYLOR_V3_0_3("Taylor_V3.0.3", 51),
        TAYLOR_FORCE_BAD_DATA_TEST("gatt_server_update", 52),
        TKL_V3_0_2("TKL_V3.0.2", 100),
        TKL_V3_0_3("TKL_V3.0.3", 101),
        TKL_V3_0_3_1_DOWNGRADE("TKL_3.0.3.1_downgrade", 102),
        TKL_V4_0_6("TKL_V4.0.6", 103),
        TKL_V4_0_8("TKL_V4.0.8", 104),
        TKL_V4_0_9("TKL_V4.0.9", 105),
        BLUSTREAM_V3_0_2("Blustream_V3.0.2", 150),
        BLUSTREAM_V3_0_3("Blustream_V3.0.3", 151),
        BLUSTREAM_V4_0_9("Blustream_V4.0.9", 152),
        BLUSTREAM_V4_0_10("Blustream_V4.0.10", 153);


        private String name;
        private int id;

        private static final Map<Integer, firmwareVersions> intToTypeMap = new HashMap<>();
        static {
            for (firmwareVersions type : firmwareVersions.values()) {
                intToTypeMap.put(type.id, type);
            }
        }
        private static final Map<String, firmwareVersions> stringToTypeMap = new HashMap<>();
        static {
            for (firmwareVersions type : firmwareVersions.values()) {
                stringToTypeMap.put(type.name, type);
            }
        }

        public static firmwareVersions fromInt(int i) {
            firmwareVersions type = intToTypeMap.get(i);
            if (type == null) {
                return firmwareVersions.UNKNOWN;
            }
            return type;
        }

        public static firmwareVersions fromString(String name) {
            firmwareVersions type = stringToTypeMap.get(name);
            if (type == null) {
                return firmwareVersions.UNKNOWN;
            }
            return type;
        }

        firmwareVersions(String name, int id) {
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
            for (firmwareVersions defs : firmwareVersions.values()) {
                if (defs.id == id) {
                    return defs.name;
                }
            }
            return null;
        }

        public int getId(String name) {
            for (firmwareVersions def : firmwareVersions.values()) {
                if (def.name.equalsIgnoreCase(name)) {
                    return def.id;
                }
            }

            return -1;
        }

    }
}
