package com.esenlermotionstar.nogate.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import com.esenlermotionstar.nogate.DataManager.NoGateDb;

import java.util.ArrayList;
import java.util.Set;

public class TimeLimitManager {
    private static final String PREFERENCE_DEFAULT_NAME = "NoGatePrefs";
    private static final double ALLOW_AFTER_POSITION_VIDEO = .75;

    public static void EnableTimeLimit(Context context, Boolean newValue) {
        Settings.setTimeLimitEnabled(context, newValue);
        if (onEnabledTimeLimit != null) onEnabledTimeLimit.run(newValue);
    }

    public static int setTimeLimitMins(Context context, int newValue) {
        //int oldVal = getTimeLimitMins(context);

        int newValBetter = (newValue > 0) ? newValue : 60;
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_DEFAULT_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("time_limit_set", newValBetter).apply();
        RunOnChangeTimeLimits(NoGateDb.getTodayWatchingTimeMinutes(), NoGateDb.getTodayMaxMinutes(), getTimeLimitMins(context));
        return newValBetter;
    }

    public static int getTimeLimitMins(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_DEFAULT_NAME, Context.MODE_PRIVATE);
        if (prefs.contains("time_limit_set")) {
            return prefs.getInt("time_limit_set", 60);
        } else
            return 60;
    }

    public static int getTodayTimeLimit() {
        return NoGateDb.getTodayMaxMinutes();
    }

    public static void updateTimeLimitToDb(Context context) {
        NoGateDb.setTodayTimeLimit(getTimeLimitMins(context));
        RunOnChangeTimeLimits(NoGateDb.getTodayWatchingTimeMinutes(), NoGateDb.getTodayMaxMinutes(), getTimeLimitMins(context));
    }


    public static void setDatabaseTimeLimit(int minsTimeLimit, Context context) {
        NoGateDb.setTodayTimeLimit(minsTimeLimit);
        RunOnChangeTimeLimits(NoGateDb.getTodayWatchingTimeMinutes(), NoGateDb.getTodayMaxMinutes(), getTimeLimitMins(context));
    }

    public static void increaseDatabaseTimeLimit(int minsTimeLimit, Context context) {
        NoGateDb.increaseTodayDatabaseTimeLimit(minsTimeLimit);
        RunOnChangeTimeLimits(NoGateDb.getTodayWatchingTimeMinutes(), NoGateDb.getTodayMaxMinutes(), getTimeLimitMins(context));
    }

    public static int getTodayWatchedTimes() {
        return NoGateDb.getTodayWatchingTimeMinutes();
    }

    public static void setMinsWatchedInToday(int i, Context ctx) {
        NoGateDb.setTodayWatchingTimeMinutes(i);

        RunOnChangeTimeLimits(i, NoGateDb.getTodayMaxMinutes(), getTimeLimitMins(ctx));
    }

    public static boolean getTimeLimitingEnabled(Context ctx) {
        return Settings.getIsTimeLimitEnabled(ctx);
    }


    public abstract static class OnChangeTimeLimitEnabled {
        abstract void run(Boolean newValue);
    }

    static OnChangeTimeLimitEnabled onEnabledTimeLimit;

    public static void setOnEnabledTimeLimit(OnChangeTimeLimitEnabled onEnabledTimeLimit) {
        TimeLimitManager.onEnabledTimeLimit = onEnabledTimeLimit;
    }

    public static OnChangeTimeLimitEnabled getOnEnabledTimeLimit() {
        return onEnabledTimeLimit;
    }


    public abstract static class OnChangeTimeLimits {
        protected abstract void run(int watched, int dbTodayTimeLimit, int defaultTimeLimit);
    }

    static ArrayList<OnChangeTimeLimits> changeTimeLimitEvents;

    public static void addOnChangeTimeLimitEvent(OnChangeTimeLimits changeTimeLimitEvent) {
        if (changeTimeLimitEvents == null) changeTimeLimitEvents = new ArrayList<>();
        changeTimeLimitEvents.add(changeTimeLimitEvent);
    }

    public static void removeOnChangeTimeLimitEvent(OnChangeTimeLimits changeTimeLimitEvent) {
        if (changeTimeLimitEvents != null && changeTimeLimitEvents.indexOf(changeTimeLimitEvent) > -1) {
            changeTimeLimitEvents.remove(changeTimeLimitEvent);
        }
    }

    static void RunOnChangeTimeLimits(int watched, int dbTodayTimeLimit, int defaultTimeLimit) {
        if (changeTimeLimitEvents != null) {
            for (int i = 0; i < changeTimeLimitEvents.size(); i++) {
                OnChangeTimeLimits x = changeTimeLimitEvents.get(i);
                if (x != null) x.run(watched, dbTodayTimeLimit, defaultTimeLimit);
            }
        }

    }

    public static boolean getAllowingEndVideo(Context appContext)
    {
        return Settings.getAllowingEndVideo(appContext);
    }

    public static boolean getAllowingEndVideo(Context appContext, int current_secs, int max_seconds)
    {
        return Settings.getAllowingEndVideo(appContext) && ((float)current_secs / max_seconds) >= ALLOW_AFTER_POSITION_VIDEO;
    }

    public static void setAllowEndVideo(Context appContext, Boolean newValue)
    {
        Settings.setAllowEndVideo(appContext,newValue);
    }

}
