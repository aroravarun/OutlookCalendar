package com.outlook.calendar;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by varunarora on 25/08/17.
 */
public class Utils {
    private static final String TAG = "Utils";
    public static final long MILLIS_IN_DAY = 86400000L;
    private static final int DAYS_PER_WEEK = 7;

    private static final long MILLIS_IN_WEEK = DAYS_PER_WEEK * MILLIS_IN_DAY;

    public static int getWeeksBeetween(Calendar date, Calendar mMinDate, int mFirstDayOfWeek) {
        if (date.before(mMinDate)) {
            throw new IllegalArgumentException("fromDate: " + mMinDate.getTime()
                    + " does not precede toDate: " + date.getTime());
        }
        long endTimeMillis = date.getTimeInMillis()
                + date.getTimeZone().getOffset(date.getTimeInMillis());
        long startTimeMillis = mMinDate.getTimeInMillis()
                + mMinDate.getTimeZone().getOffset(mMinDate.getTimeInMillis());
        long dayOffsetMillis = (mMinDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek)
                * MILLIS_IN_DAY;
        return (int) ((endTimeMillis - startTimeMillis + dayOffsetMillis) / MILLIS_IN_WEEK);
    }

    public static int getDaysBeetween(Calendar date, Calendar mMinDate) {
        if (date.before(mMinDate)) {
            throw new IllegalArgumentException("fromDate: " + mMinDate.getTime()
                    + " does not precede toDate: " + date.getTime());
        }
        long endTimeMillis = date.getTimeInMillis()
                + date.getTimeZone().getOffset(date.getTimeInMillis());
        long startTimeMillis = mMinDate.getTimeInMillis()
                + mMinDate.getTimeZone().getOffset(mMinDate.getTimeInMillis());

        return (int) ((endTimeMillis - startTimeMillis) / MILLIS_IN_DAY);
    }

    /** String for parsing dates. */
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final String DATE_FORMAT_DAY_DATE = "EEEE, MMMM dd";

    /** Date format for parsing dates. */
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
    private static final DateFormat DATE_FORMATTER_DAY_DATE = new SimpleDateFormat(DATE_FORMAT_DAY_DATE);

    /**
     * Utility method for the date format used by CalendarView's min/max date.
     *
     * @hide Use only as directed. For internal use only.
     */
    public static boolean parseDate(String date, Calendar outDate) {
        if (date == null || date.isEmpty()) {
            return false;
        }

        try {
            final Date parsedDate = DATE_FORMATTER.parse(date);
            outDate.setTime(parsedDate);
            return true;
        } catch (ParseException e) {
            Log.w(TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    public static String getDateText(Calendar date){
        StringBuilder stringBuilder = new StringBuilder();
        if(isToday(date)){
            stringBuilder.append("Today ");
        }else if(isYDay(date)){
            stringBuilder.append("Yesterday ");
        }else if(isTommorrow(date)){
            stringBuilder.append("Tommorrow ");
        }
        Date d = date.getTime();
        stringBuilder.append(DATE_FORMATTER_DAY_DATE.format(d));
        return stringBuilder.toString();
    }

    public static String getTime(Calendar date, Context context){
        SimpleDateFormat sdf;
        if(android.text.format.DateFormat.is24HourFormat(context)){
            sdf = new SimpleDateFormat("hh:mm a");
        }else{
            sdf = new SimpleDateFormat("HH:MM");
        }
        return sdf.format(date.getTime());
    }


    public static String getDuration(long diff) {
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        StringBuilder stringBuilder = new StringBuilder();
        if(diffHours > 0){
            stringBuilder.append(diffHours).append("h");
        }
        if(diffMinutes > 0){
            stringBuilder.append(diffMinutes).append("m");
        }
        return stringBuilder.toString();
    }
    public static boolean isToday(Calendar calendar) {
        return isSameDay(calendar, Calendar.getInstance());
    }
    public static boolean isYDay(Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();
        c.add(Calendar.DAY_OF_YEAR, 1);
        return isSameDay(c, Calendar.getInstance());
    }
    public static boolean isTommorrow(Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();
        c.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(c, Calendar.getInstance());
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null)
            throw new IllegalArgumentException("The dates must not be null");
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static int dpToPx(float dp, Context context)
    {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
