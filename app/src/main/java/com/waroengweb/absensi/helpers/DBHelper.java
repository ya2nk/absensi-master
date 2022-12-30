package com.waroengweb.absensi.helpers;

import android.content.Context;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.waroengweb.absensi.database.AppDatabase;

public class DBHelper {

    public static AppDatabase builder(Context context) {
        Migration MIGRATION_13_14 = new Migration(13, 14) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE dinas ADD COLUMN fotoBerkas VARCHAR");
                database.execSQL("ALTER TABLE dinas ADD COLUMN jenisDinas VARCHAR");
            }
        };

        return Room.databaseBuilder(context,
                AppDatabase.class, "MyDB").allowMainThreadQueries().addMigrations(MIGRATION_13_14).build();
    }
}
