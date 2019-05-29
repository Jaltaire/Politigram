package edu.dartmouth.cs.politigram.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

// Methods for testing if device has an internet connection.
public class InternetConnectionTester {

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            return cm.getActiveNetworkInfo();
        }
        return null;
    }

    public static boolean hasMobileConnection(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null && info.isConnected() && info.getType() == 0) return true;
        else return false;
    }

    public static boolean hasWifiConnection(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null && info.isConnected() && info.getType() == 1) return true;
        else return false;
    }

    public static boolean hasInternetConnection(Context context) {
        if (hasMobileConnection(context) || hasWifiConnection(context)) return true;
        else return false;
    }

}
