package com.waroengweb.absensi.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.waroengweb.absensi.R;

public class TimeSetting {
    private Context mcontext;

    public TimeSetting(Context context){
        this.mcontext = context;
    }
    public  boolean isTimeAutomatic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(mcontext.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(mcontext.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
    }

    private boolean isTimeZoneAutomatic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(mcontext.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(mcontext.getContentResolver(), Settings.System.AUTO_TIME_ZONE, 0) == 1;
        }
    }

    public void checkDateAndTimezoneSetting(AppCompatActivity activity)
    {
        final AppCompatActivity activity1 = activity;
        if(!isTimeAutomatic() || !isTimeZoneAutomatic()){
            Toast.makeText(mcontext, R.string.err_xxx,Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    activity1.startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 0);
                }
            }, 1500);
        }
    }
}
