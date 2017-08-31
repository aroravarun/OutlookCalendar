package com.outlook.calendar;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.outlook.calendar.calendarview.utils.CalendarUtils;
import com.outlook.calendar.view.PinnedSectionListView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.outlook.calendar.view.PinnedSectionListView.*;

public class EventsAdapter extends PinnedSectionListView.PinnedSectionListAdapter {

    public static Calendar minDate = Calendar.getInstance();
    public static Calendar maxDate = Calendar.getInstance();
    private final int VIEW_TYPE_EVENT = 0;
    private final int VIEW_TYPE_HEADER = 1;
    private Context context;
    private Calendar mTempDate;
    public EventsAdapter(Context context) {
        Utils.parseDate(MainActivity.minDate, minDate);
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);
        minDate.set(Calendar.MILLISECOND, 0);
        Utils.parseDate(MainActivity.maxDate, maxDate);
        maxDate.set(Calendar.HOUR_OF_DAY, 0);
        maxDate.set(Calendar.MINUTE, 0);
        maxDate.set(Calendar.SECOND, 0);
        maxDate.set(Calendar.MILLISECOND, 0);
        Log.d("Varun", "minDate: " + minDate.getTimeInMillis());
        Log.d("Varun","maxDate: "+maxDate.getTimeInMillis());
        mTempDate = Calendar.getInstance(TimeZone.getDefault());
        this.context = context;

    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == VIEW_TYPE_HEADER;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return Utils.getDaysBeetween(maxDate, minDate)*2;
        //Multiplied by 2 so as to show day names
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        switch (viewType){
            case VIEW_TYPE_EVENT:
                return populateEventView(convertView,position);
            case VIEW_TYPE_HEADER:
                return populateHeader(convertView,position);
        }

        return convertView;
    }

    private View populateHeader(View convertView, int position){

        if(convertView == null || !(convertView instanceof LinearLayout)){
            convertView = LayoutInflater.from(context).inflate(R.layout.events_header, null);
        }
        int currentDay = position/2;
        TextView textView = (TextView) convertView.findViewById(R.id.date);
        mTempDate.setTimeInMillis(minDate.getTimeInMillis());
        mTempDate.add(Calendar.DAY_OF_YEAR, currentDay);
        String text = Utils.getDateText(mTempDate);
        textView.setText(text);
//        textView.setTextSize(Utils.dpToPx(14, context));
//        textView.setBackgroundColor(Color.WHITE);
        convertView.setTag(mTempDate);
        return convertView;
    }

    private View populateEventView(View convertView,int position){
        EventsView eventsView;
        if(convertView == null || !(convertView instanceof EventsView)){
            convertView = new EventsView(context);
        }
        eventsView = (EventsView)convertView;
        eventsView.invalidate(position);
        return eventsView;
    }

    @Override
    public int getItemViewType(int position) {
        return position%2 == 0?VIEW_TYPE_HEADER:VIEW_TYPE_EVENT;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
