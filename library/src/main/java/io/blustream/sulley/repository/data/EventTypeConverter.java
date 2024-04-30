package io.blustream.sulley.repository.data;

import androidx.room.TypeConverter;

public class EventTypeConverter {

    @TypeConverter
    public static ProximityLogEvent.Type toType(String stringType) {
        return stringType == null ? null : ProximityLogEvent.Type.valueOf(stringType);
    }

    @TypeConverter
    public static String fromDate(ProximityLogEvent.Type type) {
        return type == null ? null : type.toString();
    }
}
