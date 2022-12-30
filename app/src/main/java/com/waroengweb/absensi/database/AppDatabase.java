package com.waroengweb.absensi.database;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.waroengweb.absensi.database.dao.AbsenDao;
import com.waroengweb.absensi.database.dao.DinasDao;
import com.waroengweb.absensi.database.dao.IjinDao;
import com.waroengweb.absensi.database.dao.PesanDao;
import com.waroengweb.absensi.database.entity.Absen;
import com.waroengweb.absensi.database.entity.Dinas;
import com.waroengweb.absensi.database.entity.Ijin;
import com.waroengweb.absensi.database.entity.Pesan;

@Database(entities = {Absen.class, Ijin.class, Dinas.class, Pesan.class},version = 13,exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract AbsenDao AbsenDao();
    public abstract IjinDao IjinDao();
    public abstract DinasDao DinasDao();
    public abstract PesanDao PesanDao();
}
