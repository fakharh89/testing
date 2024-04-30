package io.blustream.sulley.repository.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity
@TypeConverters({DateConverter.class, EventTypeConverter.class})
public class ProximityLogEvent implements Comparable<ProximityLogEvent> {
    @PrimaryKey
    private Long id;
    private String sensorSerial;
    private Date date;
    private String proximityEventType;

    public ProximityLogEvent(String sensorSerial, Date date, Type proximityEventType) {
        this.sensorSerial = sensorSerial;
        this.date = date;
        this.proximityEventType = proximityEventType.toString();
    }

    public ProximityLogEvent() {
    }

    public String getSensorSerial() {
        return sensorSerial;
    }

    public void setSensorSerial(String sensorSerial) {
        this.sensorSerial = sensorSerial;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int compareTo(ProximityLogEvent other) {
        return date.compareTo(other.date);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Type getProximityEventTypeAsEnum() {
        return Type.valueOf(proximityEventType);
    }

    public void setProximityEventTypeAsEnum(Type proximityEventType) {
        this.proximityEventType = proximityEventType.toString();
    }

    public String getProximityEventType() {
        return proximityEventType;
    }

    public void setProximityEventType(String proximityEventType) {
        this.proximityEventType = proximityEventType;
    }

    @Override
    public String toString() {
        return "ProximityLogEvent{" +
                "id=" + id +
                ", sensorSerial='" + sensorSerial + '\'' +
                ", date=" + date +
                ", proximityEventType='" + proximityEventType + '\'' +
                '}';
    }

    public enum Type {
        IBEACON, ADV, CONNECTION, DATA_RECEIVED
    }
}
