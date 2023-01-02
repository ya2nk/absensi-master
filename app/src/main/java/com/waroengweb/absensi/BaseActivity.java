package com.waroengweb.absensi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.waroengweb.absensi.helpers.TimeSetting;

public class BaseActivity extends AppCompatActivity {

    protected TimeSetting timeSetting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timeSetting = new TimeSetting(this);
        timeSetting.checkDateAndTimezoneSetting(this);
    }

    @Override protected void  onResume() {
        super.onResume();
        timeSetting.checkDateAndTimezoneSetting(this);
    }
}