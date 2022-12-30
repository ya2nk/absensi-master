package com.waroengweb.absensi;


import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Pesan;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailPesanActivity extends AppCompatActivity {

    TextView judul,dari;
    public static AppDatabase db;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pesan);
        db = Room.databaseBuilder(this,
                AppDatabase.class, "MyDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        judul = (TextView)findViewById(R.id.judul);

        dari = (TextView)findViewById(R.id.dari);
        webView = (WebView)findViewById(R.id.webView);

        Intent intent = getIntent();
        int id = intent.getIntExtra("id",0);
        Pesan pesan = db.PesanDao().getPesanRow(id);
        judul.setText(pesan.getJudul());
        String tanggalDb = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(pesan.getTanggal());

        db.PesanDao().updatePesan(pesan.getId());

        dari.setText("Dari : "+pesan.getDari()+" ("+tanggalDb+")");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL("", pesan.getPesan(), "text/html", "UTF-8", "");
    }
}