package com.waroengweb.absensi.app;


import android.content.Context;
import android.net.ConnectivityManager;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class NetworkHelper {

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();

    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }


}
