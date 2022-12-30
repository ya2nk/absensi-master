package com.waroengweb.absensi.database.dao;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.waroengweb.absensi.database.entity.Absen;

import java.util.List;

@Dao
public interface AbsenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAbsen(Absen absen);

    @Query("SELECT * FROM absen ORDER BY tanggal DESC")
    List<Absen> getAllAbsen();

    @Query("SELECT * FROM absen WHERE uploaded=0 ORDER BY tanggal DESC LIMIT 1")
    List<Absen> getAllAbsenNotUploaded();

    @Query("SELECT * FROM absen WHERE tanggal BETWEEN :dateStart AND :dateEnd ORDER BY tanggal DESC")
    List<Absen> getAllAbsenBetween(long dateStart,long dateEnd);

    @Query("SELECT nip FROM absen GROUP BY nip")
    List<String> getNip();

    @Query("UPDATE absen SET uploaded=1 WHERE id=:id")
    void updateAbsen(int id);

    @Query("SELECT * FROM absen WHERE uploaded=1 AND approved=0 ORDER BY tanggal")
    List<Absen> getAllAbsenUploadedAndNotApprove();

    @Query("UPDATE absen SET approved=1 WHERE id=:id")
    void updateAbsenApproved(int id);

    @Query("DELETE FROM absen WHERE id=:id")
    void deleteAbsen(int id);

}
