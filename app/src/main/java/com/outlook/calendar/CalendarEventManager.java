package com.outlook.calendar;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.outlook.calendar.calendarview.utils.CalendarUtils;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by varunarora on 13/08/17.
 */
public class CalendarEventManager {

    private Map<Long, Set<CalendarEvent>> calendarEventMap = new HashMap<>();
    private static CalendarEventManager calendarEventManager;
    private Map<Long, Datum> weatherMap = new HashMap<>();
    public static CalendarEventManager getCalendarEventManager() {
        if (calendarEventManager == null) {
            synchronized (CalendarEventManager.class) {
                if (calendarEventManager == null) {
                    calendarEventManager = new CalendarEventManager();
                }
            }
        }
        return calendarEventManager;
    }


    public void getRawCalendarEvents(Context context) {
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION,
                                CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.EVENT_LOCATION, CalendarContract.Events.EVENT_TIMEZONE}, null,
                        null, null);
        List<CalendarEvent> calendarEvents = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                CalendarEvent calendarEvent = new CalendarEvent();
                calendarEvent.setDescription(cursor.getString(2));
                calendarEvent.setTitle(cursor.getString(1));
                calendarEvent.setStartDate(cursor.getLong(3));
                calendarEvent.setEndDate(cursor.getLong(4));
                calendarEvent.setLocation(cursor.getString(5));
                if (!TextUtils.isEmpty(cursor.getString(6))) {
                    TimeZone timeZone = TimeZone.getTimeZone(cursor.getString(6));
                    Log.d("Varun", "timezone: "+timeZone.getID());
                    calendarEvent.setTimezone(timeZone.getRawOffset());

                }
                if((calendarEvent.getEndDate() - calendarEvent.getStartDate())== CalendarUtils.DAY_IN_MILLIS){
                    calendarEvent.setSingleDayEvent(true);
                }
                calendarEvents.add(calendarEvent);
                addToEventMap(calendarEvent);
            }
        } catch (Exception e) {

        } finally {
            cursor.close();
        }
    }

    public void addToEventMap(CalendarEvent event) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getStartDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        Log.d("Varun", "adding event:" + event.toString());
        Set<CalendarEvent> calendarEvents = calendarEventMap.get(time);
        if(calendarEvents == null){
            calendarEvents = new HashSet<>();
        }
        calendarEvents.add(event);
        calendarEventMap.put(calendar.getTimeInMillis(), calendarEvents);
        if(event.isSingleDayEvent() || event.isWeatherEvent()){
            return;
        }
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        while(event.getStartDate() < calendar.getTimeInMillis() && event.getEndDate() > calendar.getTimeInMillis()){
            calendarEvents = calendarEventMap.get(calendar.getTimeInMillis());
            if(calendarEvents == null){
                calendarEvents = new HashSet<>();
            }
            calendarEvents.add(event);
            calendarEventMap.put(calendar.getTimeInMillis(), calendarEvents);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

//    private void setToLocalTimezone(CalendarEvent event) {
//        long startDate = event.getStartDate();
//        long endDate = event.getEndDate();
//        TimeZone fromTimeZone = TimeZone.getTimeZone(event.getTimezone());
//        TimeZone toTimeZone = TimeZone.getDefault();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(startDate);
//        CalendarUtils.converToTimeZone(fromTimeZone, toTimeZone, calendar);
//        event.setStartDate(calendar.getTimeInMillis());
//        calendar.setTimeInMillis(endDate);
//        CalendarUtils.converToTimeZone(fromTimeZone, toTimeZone, calendar);
//        event.setEndDate(calendar.getTimeInMillis());
//    }


    public Set<CalendarEvent> getCalendarEventsForDay(long time){
        Set<CalendarEvent> calendarEvents = calendarEventMap.get(time);
        return calendarEvents;
    }

    public  Datum getWeatherForecast(long time) {
        return weatherMap.get(time);
    }

    public void setWeatherForecast(WeatherForecast weatherForecast){

        Hourly hourly = weatherForecast.getHourly();
        if(hourly != null && hourly.getData() != null){
            for (Datum datum : hourly.getData()){
//                Calendar c =Calendar.getInstance();
//                c.setTimeInMillis(datum.getTime());
//                c = CalendarUtils.getCalendarForLocale(c, Locale.getDefault());
                weatherMap.put(datum.getTime()*1000, datum);
            }
        }
    }
//    private com.google.api.services.calendar.Calendar getCalendarService(GoogleAccountCredential credential) {
//        return new com.google.api.services.calendar.Calendar.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
//    }
}
