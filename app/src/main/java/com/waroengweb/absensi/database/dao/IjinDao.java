package com.waroengweb.absensi.database.dao;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.waroengweb.absensi.database.entity.Ijin;

import java.util.List;


@Dao
public interface IjinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertIjin(Ijin ijin);

    @Query("SELECT * FROM ijin WHERE jenisIjin != 'Cuti'  ORDER BY tanggal DESC")
    List<Ijin> getAllIjin();

    @Query("SELECT * FROM ijin WHERE jenisIjin='Cuti' ORDER BY tanggal DESC")
    List<Ijin> getAllCuti();

    @Query("SELECT * FROM ijin WHERE uploaded = 0 AND jenisIjin != 'Cuti' ORDER BY tanggal DESC LIMIT 1")
    List<Ijin> getAllIjinNotUploaded();

    @Query("SELECT * FROM ijin WHERE uploaded = 0 AND jenisIjin = 'Cuti' ORDER BY tanggal DESC")
    List<Ijin> getAllCutiNotUploaded();

    @Query("SELECT * FROM ijin WHERE tanggal BETWEEN :dateStart AND :dateEnd AND jenisIjin != 'Cuti' ORDER BY tanggal DESC")
    List<Ijin> getAllIjinBetween(long dateStart,long dateEnd);

    @Query("SELECT * FROM ijin WHERE tanggal BETWEEN :dateStart AND :dateEnd AND jenisIjin = 'Cuti' ORDER BY tanggal DESC")
    List<Ijin> getAllCutiBetween(long dateStart,long dateEnd);

    @Query("SELECT * FROM ijin WHERE uploaded=1 AND approved=0 ORDER BY tanggal")
    List<Ijin> getAllIjinUploadedAndNotApprove();

    @Query("UPDATE ijin SET uploaded=1 WHERE id=:id")
    void updateIjin(int id);

    @Query("UPDATE ijin SET approved=1 WHERE id=:id")
    void updateIjinApproved(int id);

    @Query("DELETE FROM ijin WHERE id=:id")
    void deleteIjin(int id);

    @Query("SELECT id FROM ijin WHERE approved=0")
    List<Integer> getAllIdIjin();
}
