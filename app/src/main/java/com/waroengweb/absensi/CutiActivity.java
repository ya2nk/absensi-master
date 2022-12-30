package com.waroengweb.absensi;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.waroengweb.absensi.adapter.IjinAdapter;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.database.entity.Ijin;
import com.waroengweb.absensi.helpers.DBHelper;
import com.waroengweb.absensi.helpers.UploadData;
import com.waroengweb.absensi.radioButton.PresetRadioGroup;
import com.waroengweb.absensi.radioButton.PresetValueButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CutiActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    PresetRadioGroup mSetDurationPresetRadioGroup;
    IjinAdapter cutiAdapter;
    List<Ijin> cutiList = new ArrayList<>();
    public static AppDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuti);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("DATA CUTI TAHUNAN");
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycleViewContainer2);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

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
                    case "30 Hari" :
                        days = -30;
                        break;
                    case "Semua" :
                        days = 0;
                        break;
                    default :
                        days = -30;
                        break;
                }

                prepareCutiData(days);
            }
        });

        prepareCutiData(-30);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CutiActivity.this,InputCutiActivity.class);
                startActivity(intent);
            }
        });
    }

    public void prepareCutiData(int days)
    {
        if (days == 0){
            cutiList = db.IjinDao().getAllCuti();
            //absenList.clear();
        } else {
            String dateFormat = "yyyy-MM-dd";
            SimpleDateFormat format = new SimpleDateFormat(dateFormat);
            Date dateStart;
            try {
                dateStart = format.parse(getCalculatedDate(dateFormat, days));
                cutiList = db.IjinDao().getAllCutiBetween(dateStart.getTime(), new Date().getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        

        cutiAdapter = new IjinAdapter(this,cutiList);
        recyclerView.setAdapter(cutiAdapter);
        cutiAdapter.notifyDataSetChanged();
        runLayoutAnimation(recyclerView);
    }

    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
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
                new UploadData(this,this).setUploadData("cuti");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        prepareCutiData(-30);
    }





}
