package io.blustream.sulley.repository.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogDao {
    @Query("SELECT * FROM proximityLogEvent")
    List<ProximityLogEvent> getAll();

    @Insert
    void insertAll(ProximityLogEvent... events);

    @Insert
    void insert(ProximityLogEvent event);

    @Delete
    void delete(ProximityLogEvent event);

    @Query("DELETE FROM proximityLogEvent")
    void nukeTable();

}
