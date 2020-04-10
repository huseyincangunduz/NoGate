package com.esenlermotionstar.nogate.DataManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.esenlermotionstar.nogate.Helper.Settings;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import static com.esenlermotionstar.nogate.TimeLimitingDialog.PREFERENCE_DEFAULT_NAME;


public class NoGateDb extends SQLiteOpenHelper {


    /*
          __  _  _ __     ____    __    __   __  ___  __
        /' _/| || |  \__ / _/ |  /  \ /' _//' _/| __/' _/
        `._`.| \/ | -<__| \_| |_| /\ |`._`.`._`.| _|`._`.
        |___/ \__/|__/   \__/___|_||_||___/|___/|___|___/



         */
    static class DateToday {
        public DateToday() {
            Calendar cal = Calendar.getInstance();
            day = cal.get(Calendar.DATE);
            month = cal.get(Calendar.MONTH);
            year = cal.get(Calendar.YEAR);
        }

        public int day, month, year;

        public String[] dataStrings() {
            return new String[]
                    {String.valueOf(day),
                            String.valueOf(month), String.valueOf(year)};
        }
    }

    public static class TodayInfos {
        public DateToday today;

        public int WatchedTimesMin, TimeLimitMin;

        public TodayInfos(DateToday today_, int watched, int limit) {
            today = today_;
            WatchedTimesMin = watched;
            TimeLimitMin = limit;
        }

        public String[] dataStrings() {
            return new String[]
                    {String.valueOf(today.day),
                            String.valueOf(today.month), String.valueOf(today.year)
                            ,  String.valueOf(WatchedTimesMin), String.valueOf(TimeLimitMin)};
        }
    }


/*
 _ __  _   __ _____ __  __  _  ______
| |  \| |/' _/_   _/  \|  \| |/ _/ __|
| | | ' |`._`. | || /\ | | ' | \_| _|
|_|_|\__||___/ |_||_||_|_|\__|\__/___|
                                      _   __
 | __| || |  \| |/ _/_   _| |/__\|  \| |/' _/
 | _|| \/ | | ' | \__ | | | | \/ | | ' |`._`.
 |_|  \__/|_|\__|\__/ |_| |_|\__/|_|\__||___/ */

    Context appContext;

    NoGateDb(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        appContext = context;
        Log.i(null, "NoGateDb oluşturucusu tamamlandı");
    }


    @Override
    public void onOpen(SQLiteDatabase db) {

        super.onOpen(db);
        Log.i(null, "onOpen: Tetiklendi");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String creationDb =
                "CREATE TABLE \"WatchingTimesLogs\" " +
                        "(\"day\" INTEGER, " +
                        "\"month\" INTEGER, " +
                        "\"year\" INTEGER," +
                        "\"watchingTime\" INTEGER," +
                        "\"timeLimit\" INTEGER);";


        db.execSQL(creationDb);
        /* */
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    TodayInfos getTodaysData(SQLiteDatabase db) {

        DateToday today = new DateToday();

        Cursor c = db.rawQuery("SELECT watchingTime,timeLimit FROM WatchingTimesLogs WHERE day=? and month=? and year=?",today.dataStrings());
        if (c.getCount() > 0) {
            c.moveToFirst();
            int izlenen = c.getInt(c.getColumnIndex("watchingTime"));
            int max = c.getInt(c.getColumnIndex("timeLimit"));
            return (new TodayInfos(today,izlenen,max));
        } else {
            TodayInfos infos = new TodayInfos(today,0,getDefaultTimeLimit(appContext));
            db.execSQL("INSERT INTO WatchingTimesLogs(\"day\",\"month\",\"year\",\"watchingTime\",\"timeLimit\") VALUES (?,?,?,?,?)",infos.dataStrings());
            return infos;
        }
    }

    //set
    private void setTodayWatchingTimeMinutes(int newMinute, SQLiteDatabase writableDatabase) {
        DateToday today = new DateToday();
        String[] dataStrings = today.dataStrings();
        writableDatabase.execSQL("UPDATE WatchingTimesLogs SET watchingTime = ? WHERE day=? and month=? and year=?",
                new String[] {String.valueOf(newMinute), ""+today.day, ""+today.month,""+today.year});

    }
    private void setTimeLimit(int newMinute, SQLiteDatabase writableDatabase) {
        DateToday today = new DateToday();
        TodayInfos inf  = new TodayInfos(today,0,newMinute);
        writableDatabase.execSQL("UPDATE WatchingTimesLogs SET timeLimit = ? WHERE day=? and month=? and year=?",
                new Object[]{
                        inf.TimeLimitMin,inf.today.day, inf.today.month, inf.today.year
                });
    }

    private int getTodayWatchingTimeMinutes(SQLiteDatabase readableDb)
    { return getTodaysData(readableDb).WatchedTimesMin;
    }
    private int getTodaysMaxWatchingTimeMinutes(SQLiteDatabase readableDb)
    {
        return getTodaysData(readableDb).TimeLimitMin;
    }

    private void increaseLimit(int increaseMinutes, SQLiteDatabase writableDatabase) {
        TodayInfos inf = getTodaysData(writableDatabase);
        inf.TimeLimitMin = inf.TimeLimitMin + increaseMinutes;

        writableDatabase.execSQL("UPDATE WatchingTimesLogs SET timeLimit = ? WHERE day=? and month=? and year=?",
                new Object[]{
                inf.TimeLimitMin,inf.today.day, inf.today.month, inf.today.year
        });
    }


    /*
  __ _____ __ _____ _  ___  ___ _  _ __  _  ________ _  __  __  _   __
/' _/_   _/  \_   _| |/ _/ | __| || |  \| |/ _/_   _| |/__\|  \| |/' _/
`._`. | || /\ || | | | \__ | _|| \/ | | ' | \__ | | | | \/ | | ' |`._`.
|___/ |_||_||_||_| |_|\__/ |_|  \__/|_|\__|\__/ |_| |_|\__/|_|\__||___/
    */


    static NoGateDb instance = null;
    public static void increaseTodayDatabaseTimeLimit(int minsTimeLimit) {
        instance.increaseLimit(minsTimeLimit,instance.getWritableDatabase());
    }

    public static int getDefaultTimeLimit(Context appContext)
    {
        return Settings.getDefaultTimeLimit(appContext);
    }
    public static NoGateDb getInstance(Context applicationContext) {
        if (instance == null){
            instance = new NoGateDb(applicationContext, "nogatedb", null, 1);
            FirstRunTimeLimitDBCleanHelper.TimeLimitClearControl(applicationContext,instance.getWritableDatabase());
        }
        return instance;
    }

    public static int getTodayWatchingTimeMinutes() {
        if (instance == null) throw new NullPointerException("NoGate database is not initialized.");
        else {
            return instance.getTodayWatchingTimeMinutes(instance.getReadableDatabase());
        }
    }
    public static int getTodayMaxMinutes() {
        if (instance == null) throw new NullPointerException("NoGate database is not initialized.");
        else {
            return instance.getTodaysMaxWatchingTimeMinutes(instance.getReadableDatabase());
        }
    }


    public abstract static class OnWatchingMinutesAreUpdated {
        public abstract void run(int min, int max);
    }

    static OnWatchingMinutesAreUpdated onWatchingMinutesAreUpdated;

    public static void setOnWatchingMinutesAreUpdated(OnWatchingMinutesAreUpdated onWatchingMinutesAreUpdated_) {
        onWatchingMinutesAreUpdated = onWatchingMinutesAreUpdated_;
    }

    public static OnWatchingMinutesAreUpdated getOnWatchingMinutesAreUpdated() {
        return onWatchingMinutesAreUpdated;
    }


    public static void setTodayWatchingTimeMinutes(int newMinute) {
        if (instance == null) throw new NullPointerException("NoGate database is not initialized.");
        else {
            instance.setTodayWatchingTimeMinutes(newMinute, instance.getWritableDatabase());

            OnWatchingMinutesAreUpdated x = getOnWatchingMinutesAreUpdated();
            if (x != null) x.run(newMinute,getTodayMaxMinutes());
        }
    }
    public static void setTodayTimeLimit(int newMinute_)
    {
        if (instance == null) throw new NullPointerException("NoGate database is not initialized.");
        else {
            instance.setTimeLimit(newMinute_, instance.getWritableDatabase());

        }
    }

}
