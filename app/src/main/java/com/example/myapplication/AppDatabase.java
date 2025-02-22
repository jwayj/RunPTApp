package com.example.myapplication;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities= {RecordData.class},version=2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecordDataDao RecordDataDao();
}