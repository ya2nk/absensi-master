package com.waroengweb.absensi.database.entity;



import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName="dinas")
public class Dinas {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String nip;
    private String foto;
    private int uploaded;
    private int approved;
    private Date tanggal,tanggal2;
    private String typeDinas;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public Date getTanggal2() {
        return tanggal2;
    }

    public void setTanggal2(Date tanggal2) {
        this.tanggal2 = tanggal2;
    }

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public String getTypeDinas() {
        return typeDinas;
    }

    public void setTypeDinas(String typeDinas) {
        this.typeDinas = typeDinas;
    }
}
