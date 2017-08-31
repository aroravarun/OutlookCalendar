package com.outlook.calendar;

import android.text.TextUtils;

/**
 * Created by varunarora on 13/08/17.
 */
public class CalendarEvent {
    private String id;
    private long startDate;
    private long endDate;
    private String title;
    private String description;
    private String location;
    private int timezone;

    public void setWeatherEvent(boolean weatherEvent) {
        isWeatherEvent = weatherEvent;
    }

    private boolean isWeatherEvent;
    public boolean isSingleDayEvent() {
        return isSingleDayEvent;
    }

    public void setSingleDayEvent(boolean singleDayEvent) {
        isSingleDayEvent = singleDayEvent;
    }

    private boolean isSingleDayEvent;
    public String getLocation() {
        return location;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof CalendarEvent)){
            return false;
        }
        CalendarEvent calendarEvent = (CalendarEvent)obj;
        if(!TextUtils.equals(this.title,calendarEvent.getTitle())){
            return false;
        }
        if(!TextUtils.equals(this.description,calendarEvent.getDescription())){
            return false;
        }
        if(this.startDate != calendarEvent.getStartDate()){
            return false;
        }
        if(this.endDate != calendarEvent.getEndDate()){
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result =!TextUtils.isEmpty(title)? 31 * result + title.hashCode():result;
        result = 31 * result + (int)startDate;
        result = 31 * result + (int)endDate;
        result =!TextUtils.isEmpty(description)? 31 * result + description.hashCode():result;
        return result;
    }
    @Override
    public String toString() {
        return "CalendarEvent: "+"title: "+title+" description: "+description+" startDate: "+startDate+" endDate: "+endDate+" timezone: "+timezone;
    }

    public boolean isWeatherEvent() {
        return isWeatherEvent;
    }
}
