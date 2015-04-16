package com.blin.btrack.UPDATE;

/**
 * Created by blin on 2015/4/14.
 */

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.blin.btrack.R;

public class UpdateConfig {
    private static final String TAG = "Config";

    public static final String UPDATE_SERVER = "http://60.248.68.66/update/";
    public static final String UPDATE_APKNAME = "btrack.apk";
    public static final String UPDATE_VERJSON = "ver.json";
    public static final String UPDATE_SAVENAME = "btrack.apk";


    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(
                    "com.example.blin.updatesample", 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verCode;
    }

    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(
                    "com.blin.btrack", 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verName;

    }

    public static String getAppName(Context context) {
        String verName = context.getResources()
                .getText(R.string.app_name).toString();
        return verName;
    }
}

