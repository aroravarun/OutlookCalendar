package com.outlook.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.outlook.calendar.calendarview.utils.CalendarUtils;
import com.outlook.calendar.calendarview.utils.CalendarViewLegacyDelegate;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Created by varunarora on 26/08/17.
 */
public class EventsView extends LinearLayout{
    private Context context;
    LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    public EventsView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        this.context = context;
        LinearLayout linearLayout =(LinearLayout) LayoutInflater.from(context).inflate(R.layout.events_view, null);
        addView(linearLayout);
    }

    public EventsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EventsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    public void invalidate(int position){
        removeAllViews();
        int day = position/2;
        long millis = day * CalendarViewLegacyDelegate.MILLIS_IN_DAY + EventsAdapter.minDate.getTimeInMillis();
        Set<CalendarEvent> calendarEvents = CalendarEventManager.getCalendarEventManager().getCalendarEventsForDay(millis);
        boolean isOnlyWeatherEvent = false;
        if(calendarEvents != null && !calendarEvents.isEmpty()){
//            if(getChildCount() > calendarEvents.size()){
//                removeViews(calendarEvents.size(), getChildCount()-calendarEvents.size());
//            }else {
//                for(int i = 0 ; i <calendarEvents.size() -getChildCount();i++){
//                    addView(LayoutInflater.from(context).inflate(R.layout.events_view, null));
//                }
//            }
            int i =0;
            for(CalendarEvent event : calendarEvents){
                if (event.isWeatherEvent()) {
                    isOnlyWeatherEvent = true;
                    LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.weather_layout, null);
                    addView(root);
                    TextView textViewMorning = (TextView) root.findViewById(R.id.morningtemp);
                    TextView textViewAfternoon = (TextView) root.findViewById(R.id.afternoontemp);
                    TextView textViewEvening = (TextView) root.findViewById(R.id.eveningtemp);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(event.getStartDate());
                    long morning = CalendarUtils.getMorning(c);
                    Datum datum = CalendarEventManager.getCalendarEventManager().getWeatherForecast(morning);
                    if (datum != null) {
                        textViewMorning.setText(datum.getApparentTemperature().toString()+" F");
                    } else {
                        root.findViewById(R.id.morning).setVisibility(GONE);
                    }
                    long afternoon = CalendarUtils.getAfternoon(c);
                    datum = CalendarEventManager.getCalendarEventManager().getWeatherForecast(afternoon);
                    if (datum != null) {
                        textViewAfternoon.setText(datum.getApparentTemperature().toString()+" F");
                    } else {
                        root.findViewById(R.id.afternoon).setVisibility(GONE);
                    }
                    long evening = CalendarUtils.getEvening(c);
                    datum = CalendarEventManager.getCalendarEventManager().getWeatherForecast(evening);
                    if (datum != null) {
                        textViewEvening.setText(datum.getApparentTemperature().toString()+" F");
                    } else {
                        root.findViewById(R.id.evening).setVisibility(GONE);
                    }
                }else{
                    isOnlyWeatherEvent = false;
                    LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.events_view, null);
                    addView(root);
                    root.findViewById(R.id.content).setVisibility(VISIBLE);
                    root.findViewById(R.id.no_events).setVisibility(GONE);
                    TextView textView = (TextView)root.findViewById(R.id.title);
                    textView.setText(event.getTitle());
                    if(event.isSingleDayEvent()){
                        textView = (TextView)root.findViewById(R.id.time);
                        textView.setText("ALL DAY");
                        textView = (TextView)root.findViewById(R.id.duration);
                        textView.setText("1d");
                    }else{
                        textView = (TextView)root.findViewById(R.id.time);
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(event.getStartDate());
                        textView.setText(Utils.getTime(c, context));
                        textView = (TextView)root.findViewById(R.id.duration);
                        textView.setText(Utils.getDuration(event.getEndDate() - event.getStartDate()));
                    }
                    textView = (TextView)root.findViewById(R.id.location);
                    textView.setText(event.getLocation());
                    if(++i == calendarEvents.size()){
                        root.findViewById(R.id.seperator).setVisibility(GONE);
                    }
                }
            }
        }

        if(calendarEvents == null || calendarEvents.isEmpty() || isOnlyWeatherEvent){
            LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.events_view, null);
            addView(root);
            findViewById(R.id.content).setVisibility(GONE);
            findViewById(R.id.no_events).setVisibility(VISIBLE);
            root.findViewById(R.id.seperator).setVisibility(GONE);
        }
    }
}
