package com.waroengweb.absensi.database.entity;



import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName="absen")
public class Absen {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String nip;
    private String foto;
    private int uploaded;
    private Date tanggal;
    private String tanggalReal;
    private String tanggalServer;
    private Double latitude,longitude;
    @ColumnInfo(name="hp_model")
    private String hpModel;
    private int approved;
    private String timezone;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setHpModel(String hpModel) {
        this.hpModel = hpModel;
    }

    public String getHpModel() {
        return hpModel;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public int getApproved() {
        return approved;
    }

    public void setTanggalReal(String tanggalReal) {
        this.tanggalReal = tanggalReal;
    }

    public String getTanggalReal() {
        return tanggalReal;
    }

    public String getTanggalServer() {
        return tanggalServer;
    }

    public void setTanggalServer(String tanggalServer) {
        this.tanggalServer = tanggalServer;
    }



    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
