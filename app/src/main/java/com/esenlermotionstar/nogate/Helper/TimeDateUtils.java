package com.esenlermotionstar.nogate.Helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeDateUtils {
    public static String getTodayDate()
    {
        Date c = Calendar.getInstance().getTime();
        DateFormat format = new SimpleDateFormat("dd MM yyyy");
        return format.format(c);

    }

}
