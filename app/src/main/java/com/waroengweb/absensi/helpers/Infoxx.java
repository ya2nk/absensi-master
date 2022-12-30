package com.waroengweb.absensi.helpers;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class Infoxx {
    private Context mcontext;

    public Infoxx(Context context){
        this.mcontext = context;
    }
    public  boolean isTimeAutomatic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(mcontext.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(mcontext.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
    }

    public boolean isTimeZoneAutomatic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(mcontext.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(mcontext.getContentResolver(), Settings.System.AUTO_TIME_ZONE, 0) == 1;
        }
    }
}
