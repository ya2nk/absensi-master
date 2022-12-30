package com.waroengweb.absensi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.room.Room;

import com.tapadoo.alerter.Alerter;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.helpers.DBHelper;
import com.waroengweb.absensi.helpers.GetAsyncPesan;
import com.waroengweb.absensi.helpers.Session;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    GridLayout mainGrid;
    private String[] permissionRequest;
    int PERMISSION_ID = 444;
    public static AppDatabase db;
    TextView counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainGrid = (GridLayout) findViewById(R.id.mainGrid);
        setSingleEvent(mainGrid);

        permissionRequest = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };

        requestPermissions(permissionRequest);

        openDialog();

        db = DBHelper.builder(this);

        getPesan();

       setCounter();
    }

    public void setCounter()
    {
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        Date dateStart;
        int jumlahPesan = 0;
        try {
            dateStart = format.parse(getCalculatedDate(dateFormat, -30));
            jumlahPesan = db.PesanDao().getUnreadPesan(dateStart.getTime(), new Date().getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        counter = (TextView)findViewById(R.id.counter);
        counter.setText(String.valueOf(jumlahPesan));
        if (jumlahPesan == 0){
            counter.setVisibility(View.INVISIBLE);
        } else {
            counter.setVisibility(View.VISIBLE);
        }
    }



    private void setSingleEvent(GridLayout mainGrid) {
        //Loop all child item of Main Grid
        for (int i = 0; i < mainGrid.getChildCount(); i++) {
            //You can see , all child item is CardView , so we just cast object to CardView
            CardView cardView = (CardView) mainGrid.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent;
                    if (finalI == 0){
                        //intent = new Intent(MainActivity.this,Presensi.class);
                        //startActivity(intent);
                       
                        String waktu = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        String open = "07:00";
                        Calendar calendar = Calendar.getInstance();
                        int dayWeek = calendar.get(Calendar.DAY_OF_WEEK);
                        if (dayWeek == 2){
                            open = "07:00";
                        }
                        if (checktimings(open,waktu) && checktimings(waktu,"18:00")){
                            intent = new Intent(MainActivity.this,Presensi.class);
                            startActivity(intent);
                        } else {
                            Alerter.create(MainActivity.this)
                                    .setTitle("ERROR")
                                    .setText("Tidak bisa Melakukan absen diluar jam "+open+" dan 18:00")
                                    .setBackgroundColorInt(Color.RED).show();
                        }

                    } else if(finalI == 1) {
                        intent = new Intent(MainActivity.this,DataPresensi.class);
                        startActivity(intent);
                    } else if(finalI == 2){
                        intent = new Intent(MainActivity.this,IjinActivity.class);
                        startActivity(intent);
                    } else if(finalI == 3){
                        intent = new Intent(MainActivity.this,CutiActivity.class);
                        startActivity(intent);
                    } else if(finalI == 4){
                        intent = new Intent(MainActivity.this,DinasActivity.class);
                        startActivity(intent);
                    } else {

                        if (Session.getLoginStatus(MainActivity.this)){
                            intent = new Intent(MainActivity.this,PesanActivity.class);
                        } else {
                            intent = new Intent(MainActivity.this,LoginActivity.class);
                        }
                        startActivity(intent);
                    }


                }
            });
        }
    }

    private void requestPermissions(String[] permissionRequests) {
        ActivityCompat.requestPermissions(
                this,
                permissionRequests,
                PERMISSION_ID
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    private void openDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("INFO PENTING!!");
        alertDialogBuilder.setMessage("Untuk tingkat akurasi yang tinggi, Silakan melakukan absensi di ruangan terbuka dan tanpa halangan ke satelite");
        alertDialogBuilder.setNegativeButton("Dismiss",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private boolean checktimings(String time, String endtime) {

        String pattern = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        try {
            Date date1 = sdf.parse(time);
            Date date2 = sdf.parse(endtime);

            if(date1.before(date2)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return false;
    }



    private void getPesan()
    {
        new GetAsyncPesan(this,this).doTask();
        //Toast.makeText(this,"id "+idPesans.get(0),Toast.LENGTH_SHORT).show();
        //Log.d("id","id pesan : "+idPesans);
    }

   @Override
    public void onResume()
   {
       super.onResume();
       setCounter();
       //getPesan();
   }

    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }
}
