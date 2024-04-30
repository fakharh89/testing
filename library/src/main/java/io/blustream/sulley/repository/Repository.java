package io.blustream.sulley.repository;

import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

import io.blustream.sulley.repository.data.ProximityLogEvent;

public interface Repository {

    LiveData<Boolean> getIsProximityScanning();

    void setIsProximityScanning(Boolean isProximityScanning);

    LiveData<List<ProximityLogEvent>> getLogs();

    void removeAllLogs();

    LiveData<Void> getSensorUpdated();

    void setSensorUpdated();

    void addLog(ProximityLogEvent proximityLogEvent);

    LiveData<Long> getScanDuration();

    void setScanDuration(Long scanDuration);

    LiveData<Date> getProximityStarted();

    void setProximityStarted(Date proximityStarted);
}
