package com.waroengweb.absensi.database.dao;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.waroengweb.absensi.database.entity.Dinas;

import java.util.List;

@Dao
public interface DinasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertDinas(Dinas dinas);

    @Query("SELECT * FROM dinas ORDER BY tanggal DESC")
    List<Dinas> getAllDinas();

    @Query("SELECT * FROM dinas WHERE uploaded=0 ORDER BY tanggal DESC LIMIT 1")
    List<Dinas> getAllDinasNotUploaded();

    @Query("SELECT * FROM dinas WHERE tanggal BETWEEN :dateStart AND :dateEnd ORDER BY tanggal DESC")
    List<Dinas> getAllDinasBetween(long dateStart,long dateEnd);

    @Query("UPDATE dinas SET uploaded=1 WHERE id=:id")
    void updateDinas(int id);

    @Query("SELECT * FROM dinas WHERE uploaded=1 AND approved=0 ORDER BY tanggal")
    List<Dinas> getAllDinasUploadedAndNotApprove();

    @Query("UPDATE dinas SET approved=1 WHERE id=:id")
    void updateDinasApproved(int id);

    @Query("DELETE FROM dinas WHERE id=:id")
    void deleteDinas(int id);
}
