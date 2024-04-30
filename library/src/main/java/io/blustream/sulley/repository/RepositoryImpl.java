package io.blustream.sulley.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.blustream.logger.Log;
import io.blustream.sulley.repository.data.AppDatabase;
import io.blustream.sulley.repository.data.ProximityLogEvent;

public class RepositoryImpl implements Repository {
    private static RepositoryImpl repositoryInstance;
    //cache
    private MutableLiveData<List<ProximityLogEvent>> logs = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Void> sensorUpdated = new MutableLiveData<>();
    private MutableLiveData<Long> scanDuration = new MutableLiveData<>();
    private MutableLiveData<Boolean> isProximityScanning = new MutableLiveData<>(false);
    private MutableLiveData<Date> proximityStarted = new MutableLiveData<>();

    private AppDatabase roomDb;

    private RepositoryImpl(Application application) {
        roomDb = Room.databaseBuilder(application,
                AppDatabase.class, "database-name").allowMainThreadQueries().build();
        if (logs.getValue().isEmpty()) {
            getAllLogsFromDb();
        }
    }

    public static Repository getRepository(Application application) {
        if (repositoryInstance == null) {
            repositoryInstance = new RepositoryImpl(application);
        }
        return repositoryInstance;
    }

    @Override
    public LiveData<Boolean> getIsProximityScanning() {
        return isProximityScanning;
    }

    @Override
    public void setIsProximityScanning(Boolean isProximityScanning) {
        this.isProximityScanning.postValue(isProximityScanning);
    }

    @Override
    public LiveData<List<ProximityLogEvent>> getLogs() {
        return logs;
    }

    @Override
    public void removeAllLogs() {
        roomDb.clearAllTables();
        logs.getValue().clear();
        logs.postValue(logs.getValue());
    }

    @Override
    public MutableLiveData<Void> getSensorUpdated() {
        return sensorUpdated;
    }

    @Override
    public void setSensorUpdated() {
        sensorUpdated.setValue(null);
    }

    public void addLog(ProximityLogEvent proximityLogEvent) {
        Log.d("addLog");
        if (!logs.getValue().isEmpty() && (new Date().getTime() - logs.getValue().get(logs.getValue().size() - 1).getDate().getTime() < 2000)) {
            return;
        }
        saveToDb(proximityLogEvent);
        logs.getValue().add(proximityLogEvent);  //let it throw NPE
        logs.postValue(logs.getValue()); // notify observers
        Log.d("addLog.proximityLogEvent = " + proximityLogEvent);
    }

    private void saveToDb(ProximityLogEvent proximityLogEvent) {
        roomDb.logDao().insert(proximityLogEvent);
    }

    @Override
    public LiveData<Long> getScanDuration() {
        return scanDuration;
    }

    @Override
    public void setScanDuration(Long scanDuration) {
        this.scanDuration.postValue(scanDuration);
    }

    @Override
    public LiveData<Date> getProximityStarted() {
        return proximityStarted;
    }

    @Override
    public void setProximityStarted(Date proximityStarted) {
        this.proximityStarted.postValue(proximityStarted);
    }

    private void getAllLogsFromDb() {
        List<ProximityLogEvent> result = roomDb.logDao().getAll();
        if (result.isEmpty()) return;
        List<ProximityLogEvent> list = result.subList(0, result.size() - 1);
        logs.postValue(new ArrayList<>(list));
    }
}
