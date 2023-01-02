package com.waroengweb.absensi;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.waroengweb.absensi.database.AppDatabase;
import com.waroengweb.absensi.helpers.DBHelper;
import com.waroengweb.absensi.helpers.TimeSetting;
import com.waroengweb.absensi.helpers.UploadData;
import com.waroengweb.absensi.ui.main.SectionsPagerAdapter;

public class IjinActivity extends BaseActivity {

    TimeSetting timeSetting;
    private FragmentRefreshListener fragmentRefreshListener;
    private ProgressDialog pd;
    public static AppDatabase db;
    private String url = "http://siap.kerincikab.go.id/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("DATA IZIN");
        setSupportActionBar(toolbar);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    if(getFragmentRefreshListener()!= null){
                        getFragmentRefreshListener().onRefresh();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        pd = new ProgressDialog(this);
        pd.setMessage("Uploading Data..");

        db = DBHelper.builder(this);

        timeSetting = new TimeSetting(this);
        timeSetting.checkDateAndTimezoneSetting(IjinActivity.this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        timeSetting.checkDateAndTimezoneSetting(IjinActivity.this);
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
                new UploadData(this,this).setUploadData("Ijin");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }


    public interface FragmentRefreshListener{
        void onRefresh();
    }



}