package io.blustream.sulley.repository.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ProximityLogEvent.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LogDao logDao();
}
