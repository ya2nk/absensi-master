package com.waroengweb.absensi.database.dao;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.waroengweb.absensi.database.entity.Pesan;

import java.util.List;


@Dao
public interface PesanDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertPesan(Pesan pesan);

    @Query("SELECT * FROM pesan ORDER BY idPesan DESC")
    List<Pesan> getAllPesan();

    @Query("SELECT idPesan FROM pesan ORDER BY tanggal DESC")
    List<Integer> getAllIdPesan();

    @Query("SELECT * FROM pesan WHERE tanggal BETWEEN :dateStart AND :dateEnd AND (judul LIKE '%' || :q || '%' OR pesan LIKE '%' || :q || '%') ORDER BY idPesan DESC")
    List<Pesan> getAllPesanBetween(long dateStart, long dateEnd,String q);

    @Query("UPDATE pesan SET isRead=1 WHERE id=:id")
    void updatePesan(int id);

    @Query("DELETE FROM pesan WHERE id=:id")
    void deletePesan(int id);

    @Query("SELECT * FROM pesan WHERE idPesan=:idPesan")
    Pesan getPesanRow(int idPesan);

    @Query("SELECT COUNT(*) FROM pesan WHERE isRead=0 AND tanggal BETWEEN :dateStart AND :dateEnd")
    int getUnreadPesan(long dateStart, long dateEnd);

}
