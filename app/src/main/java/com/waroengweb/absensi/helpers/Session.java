package com.waroengweb.absensi.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by OVAY on 1/4/2019.
 */

public class Session {


    static final String LOGIN_STATUS = "login_status";
    static final String USER_ID = "user_id";
    static final String ROLE = "role";
    static final String FULLNAME = "fullname";
    static final String NIP = "nip";

    public static SharedPreferences getSharedPreference(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }



    public static boolean getLoginStatus(Context context){
        return getSharedPreference(context).getBoolean(LOGIN_STATUS,false);
    }

    public static void setLoginStatus(Context context, boolean status){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(LOGIN_STATUS, status);
        editor.apply();
    }

    public static int getUserId(Context context){
        return getSharedPreference(context).getInt(USER_ID,0);
    }

    public static void setUserId(Context context, int id){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putInt(USER_ID, id);
        editor.apply();
    }

    public static String getRole(Context context){
        return getSharedPreference(context).getString(ROLE,"");
    }

    public static void setRole(Context context, String role){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(ROLE, role);
        editor.apply();
    }

    public static String getFullname(Context context){
        return getSharedPreference(context).getString(FULLNAME,"");
    }

    public static void setFullname(Context context, String fullname){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(FULLNAME, fullname);
        editor.apply();
    }

    public static String getNip(Context context){
        return getSharedPreference(context).getString(NIP,"");
    }

    public static void setNip(Context context, String nip){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(NIP, nip);
        editor.apply();
    }
}
