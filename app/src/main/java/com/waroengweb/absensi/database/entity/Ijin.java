package com.waroengweb.absensi.database.entity;



import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName="ijin")
public class Ijin {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String nip;
    private String foto;
    private int uploaded,lamaHari;
    private int approved;
    private Date tanggal,tanggal2,tanggalInput;
    private String jenisIjin;
    private String typeIjin;
    private String keterangan;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public int getUploaded() {
        return uploaded;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getNip() {
        return nip;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getFoto() {
        return foto;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggalInput(Date tanggal) {
        this.tanggalInput = tanggal;
    }

    public Date getTanggalInput() {
        return tanggalInput;
    }

    public void setTanggal2(Date tanggal) {
        this.tanggal2 = tanggal;
    }

    public Date getTanggal2() {
        return tanggal2;
    }

    public String getJenisIjin() {
        return jenisIjin;
    }

    public void setJenisIjin(String jenisIjin) {
        this.jenisIjin = jenisIjin;
    }

    public String getTypeIjin() {
        return typeIjin;
    }

    public void setTypeIjin(String typeIjin) {
        this.typeIjin = typeIjin;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public int getLamaHari() {
        return lamaHari;
    }

    public void setLamaHari(int lamaHari) {
        this.lamaHari = lamaHari;
    }
}
