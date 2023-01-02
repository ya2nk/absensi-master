package com.waroengweb.absensi;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.tapadoo.alerter.Alerter;
import com.waroengweb.absensi.adapter.AbsenAdapter;
import com.waroengweb.absensi.app.AppController;
import com.waroengweb.absensi.app.VolleyMultipartRequest;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Absen;
import com.waroengweb.absensi.helpers.DBHelper;
import com.waroengweb.absensi.helpers.Infoxx;
import com.waroengweb.absensi.radioButton.PresetRadioGroup;
import com.waroengweb.absensi.radioButton.PresetValueButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataPresensi extends BaseActivity {

    RecyclerView recyclerView;
    AbsenAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;

    List<Absen> absenList = new ArrayList<>();
    private Gson gson;
    public static AppDatabase db;
    PresetRadioGroup mSetDurationPresetRadioGroup;
    private ProgressDialog pd;
    private String url = "https://siap.kerincikab.go.id/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_presensi);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("DATA PRESENSI");
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycleViewContainer);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Absen absen = absenList.get(position);
                //new MapDialogFragment(absen).show(getSupportFragmentManager(), null);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        db = DBHelper.builder(this);


        mSetDurationPresetRadioGroup = (PresetRadioGroup) findViewById(R.id.preset_time_radio_group);

        mSetDurationPresetRadioGroup.setOnCheckedChangeListener(new PresetRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View radioGroup, View radioButton, boolean isChecked, int checkedId) {
                //Toast.makeText(DataPresensi.this,"Value : "+((PresetValueButton) radioButton).getValue(),Toast.LENGTH_SHORT).show();
                //mSetDurationEditText.setSelection(mSetDurationEditText.getText().length());
                String value = ((PresetValueButton) radioButton).getValue();
                int days;
                switch (value) {
                    case "1 Hari" :
                        days = -1;
                        break;
                    case "7 Hari" :
                        days = -7;
                        break;
                    case "30 Hari" :
                        days = -30;
                        break;
                    default :
                        days = -1;
                        break;
                }

                prepareAbsenData(days);
            }
        });

        prepareAbsenData(-1);

        checkDateAndTimezoneSetting();

        pd = new ProgressDialog(this);
        pd.setMessage("PROSES UPLOADING DATA...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.data_presensi_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_upload:
                uploadData();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    public void prepareAbsenData(int days) {

        if (days == 0){
            absenList = db.AbsenDao().getAllAbsen();
            //absenList.clear();
        } else {
            String dateFormat = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat format = new SimpleDateFormat(dateFormat);
            Date dateStart;
            try {
                dateStart = format.parse(getCalculatedDate(dateFormat, days));
                absenList = db.AbsenDao().getAllAbsenBetween(dateStart.getTime(), new Date().getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        mAdapter = new AbsenAdapter(this,absenList,this);
        recyclerView.setAdapter(mAdapter);
        //mAdapter.notifyDataSetChanged();
        runLayoutAnimation(recyclerView);

    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }

    private void uploadData()
    {

        pd.show();
        final List<Absen> listAbsen;
        listAbsen = db.AbsenDao().getAllAbsenNotUploaded();

        VolleyMultipartRequest mReq = new VolleyMultipartRequest(Request.Method.POST, url+getString(R.string.aaa),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        pd.dismiss();
                        AppController.getInstance().getRequestQueue().getCache().clear();

                        if (response.statusCode == 200){
                            String result = new String(response.data);
                            //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();

                            try {
                                JSONObject json = new JSONObject(result);
                                if (json.getString("result").equals("success")){


                                    JSONArray arrayId = json.getJSONArray("ids");
                                    if (arrayId.length() != 0){
                                        for (int i=0;i<arrayId.length();i++){
                                            int id = arrayId.getInt(i);
                                            db.AbsenDao().updateAbsen(id);
                                        }

                                        Alerter.create(DataPresensi.this).setTitle("INFO").setText("Data Berhasil Diupload")
                                                .setBackgroundColorInt(Color.GREEN).show();
                                    } else {
                                        JSONArray arrayError = json.getJSONArray("error");
                                        if (arrayError.length() == 0){
                                            Alerter.create(DataPresensi.this).setTitle("ERROR").setText("Data Gagal Diupload")
                                                    .setBackgroundColorInt(Color.RED).show();
                                        } else {
                                            Alerter.create(DataPresensi.this).setTitle("ERROR").setText(arrayError.getString(0))
                                                    .setBackgroundColorInt(Color.RED).show();
                                        }

                                    }


                                    try {
                                        JSONArray jArray = json.getJSONArray("approved");
                                        if (jArray.length() != 0){
                                            for (int i=0;i<jArray.length();i++){
                                                int id = jArray.getInt(i);
                                                db.AbsenDao().updateAbsenApproved(id);

                                            }
                                        }
                                    } catch (JSONException e){
                                        e.printStackTrace();
                                    }

                                    prepareAbsenData(-1);

                                } else {
                                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e){
                                e.printStackTrace();
                            }


                            Log.d("response :",new String(response.data));
                        } else {
                            Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                if (error instanceof NoConnectionError){
                    Toast.makeText(getApplicationContext(),"Tidak Ada Jaringan Internet",Toast.LENGTH_SHORT).show();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(),"Jaringan Internet Bermasalah Silakan coba beberapa saat lagi",Toast.LENGTH_SHORT).show();
                } else {
                //try(error.networkResponse.data!=null) {
                    String body;
                    try {
                        body = new String(error.networkResponse.data,"UTF-8");
                        Toast.makeText(getApplicationContext(),body,Toast.LENGTH_SHORT).show();
                        Log.d("Error :",body);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

            }
        }){
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                Gson gson = new Gson();
                params.put("data",gson.toJson(listAbsen));
                List<Absen> listApproved = db.AbsenDao().getAllAbsenUploadedAndNotApprove();
                params.put("data_approved",gson.toJson(listApproved));
                params.put("info",encrypt(getString(R.string.title_name)));
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();

                for (Absen absen : listAbsen){
                    long imagename = System.currentTimeMillis();


                    if (absen.getFoto() != null){
                        params.put("photo["+absen.getId()+"]", new DataPart("PIC_1_"+String.valueOf(imagename)+".jpg",convertImageToBytes(absen.getFoto())));
                    }
                }

                return params;
            }
        };

        mReq.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        AppController.getInstance().addToRequestQueue(mReq);
    }

    public  byte[] convertImageToBytes(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    public  boolean isTimeAutomatic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
    }

    private boolean isTimeZoneAutomatic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(getContentResolver(), Settings.System.AUTO_TIME_ZONE, 0) == 1;
        }
    }

    public void checkDateAndTimezoneSetting()
    {
        Infoxx xxx = new Infoxx(this);
        if(!xxx.isTimeAutomatic() || !xxx.isTimeZoneAutomatic()){
            Toast.makeText(this,getString(R.string.err_xxx),Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 0);
                }
            }, 1500);
        }
    }

    public static String encrypt(String s){

        String ascString = "";
        for (int i = 0; i < s.length(); i++){
            int ascii = (int)s.charAt(i);
            ascString += String.valueOf(Character.toChars(ascii+1));
        }
        return ascString;
    }




}
