package com.esenlermotionstar.nogate.DataManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.esenlermotionstar.nogate.Helper.TimeDateUtils;

import static com.esenlermotionstar.nogate.SettingsActivity.PREFERENCE_DEFAULT_NAME;

public class FirstRunTimeLimitDBCleanHelper {
    public static void setNoGateIsOpenedToday(Context appContext)
    {
        appContext.getSharedPreferences(PREFERENCE_DEFAULT_NAME,Context.MODE_PRIVATE).edit().putString("last_open_date", TimeDateUtils.getTodayDate()).apply();
    }
    static boolean getNoGateIsOpenedToday(Context appContext)
    {
        SharedPreferences prefs =  appContext.getSharedPreferences(PREFERENCE_DEFAULT_NAME,Context.MODE_PRIVATE);
        String date_str = "";
        if (prefs.contains("last_open_date"))
        {
            date_str = prefs.getString("last_open_date","");

            return date_str.equals(TimeDateUtils.getTodayDate());

        }
        else
            return false;
    }

    public static void TimeLimitClearControl(Context appContext, SQLiteDatabase db_writable)
    {
        if (!getNoGateIsOpenedToday(appContext))
        {
            Cursor c = db_writable.rawQuery("SELECT * FROM WatchingTimesLogs",null);
            if (c.getCount() > 7) {
                //GELECEK SÜRÜMDE DE LOG OLARAK KAYDETME OLACAK DB'Yİ
                db_writable.execSQL("DELETE FROM WatchingTimesLogs"); //
            }


            setNoGateIsOpenedToday(appContext);
        }
    }
}
