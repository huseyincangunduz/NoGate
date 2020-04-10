package com.esenlermotionstar.nogate.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TimeUtils;

import java.util.Calendar;
import java.util.Date;

import static com.esenlermotionstar.nogate.TimeLimitingDialog.PREFERENCE_DEFAULT_NAME;

public class Settings {
    private static final String PREFERENCE_DEFAULT_NAME = "NoGatePrefs";

    public static boolean getIsTimeLimitEnabled(Context ctx)
    {
        boolean value = ctx.getSharedPreferences(PREFERENCE_DEFAULT_NAME,Context.MODE_PRIVATE).getBoolean("enable_time_limit",false);
        return value;
    }




    public static int getDefaultTimeLimit(Context appContext) {
        SharedPreferences prefs =  appContext.getSharedPreferences(PREFERENCE_DEFAULT_NAME,Context.MODE_PRIVATE);
        if (prefs.contains("time_limit_set"))
        {
            return prefs.getInt("time_limit_set", 60);
        }
        else
            return  60;
    }

    public static void setTimeLimitEnabled(Context context, Boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_DEFAULT_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("enable_time_limit", newValue).apply();
    }





    public static boolean getAllowingEndVideo(Context appContext)
    {
        SharedPreferences prefs =  appContext.getSharedPreferences(PREFERENCE_DEFAULT_NAME,Context.MODE_PRIVATE);
        if (prefs.contains("allow_to_end_video"))
        {
            return (prefs.getBoolean("allow_to_end_video", false));
        }
        else
            return  false;
    }

    public static void setAllowEndVideo(Context appContext, Boolean newValue)
    {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFERENCE_DEFAULT_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("allow_to_end_video", newValue).apply();
    }


}
