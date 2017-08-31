package com.outlook.calendar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.outlook.calendar.calendarview.utils.CalendarUtils;
import com.outlook.calendar.calendarview.utils.CalendarViewCustom;
import com.outlook.calendar.view.PinnedSectionListView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, Callback<WeatherForecast>,PinnedSectionListView.PinChangeCallback {

    PinnedSectionListView pinnedSectionListView;
    CalendarViewCustom calendarViewCustom;
    public static String minDate = "05/01/2017";
    public static String maxDate = "12/31/2017";

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    EventsAdapter eventsAdapter;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_PERMISSION_GET_CALENDAR = 1004;
    static final int REQUEST_PERMISSION_GET_LOCATION = 1005;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fetchCalendarEvent();
        calendarViewCustom = (CalendarViewCustom)findViewById(R.id.calendar);

//        Retrofit client = RetrofitClient.getClient("https://holidayapi.com");
//        APIService apiService = client.create(APIService.class);

        mProgress = new ProgressDialog(this);

//        Call<HolidaysResult> call = apiService.getHolidays("IN",FETCH_HOLIDAYS_API_KEY, "2016");
//        call.enqueue(this);

        // Initialize credentials and service object.

    }


    private void syncGoogleAccount(){
        mProgress.setTitle("Google Sync");
        mProgress.setMessage("Syncing google events");
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        getResultsFromApi();
    }
    private void setEvents(){
        pinnedSectionListView = (PinnedSectionListView)findViewById(R.id.eventsList);
        eventsAdapter = new EventsAdapter(this);
        pinnedSectionListView.setAdapter(eventsAdapter);
        pinnedSectionListView.setPinChangeCallback(this);
        pinnedSectionListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(i != AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    calendarViewCustom.decreseListViewHeight();
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
        setInitialSelectionOfListView();
    }
    private void setInitialSelectionOfListView(){
        pinnedSectionListView.clearFocus();
        pinnedSectionListView.post(new Runnable() {
            @Override
            public void run() {
                int position = Utils.getDaysBeetween(java.util.Calendar.getInstance(), EventsAdapter.minDate)*2;
                pinnedSectionListView.setSelection(position);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.weather) {
            fetchLocation();
            return true;
        }else if(id == R.id.googleSync){
            syncGoogleAccount();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void fetchWeather(Location location){
        mProgress.setTitle("Weather Updates");
        mProgress.setMessage("getting weather updates");
        mProgress.show();
        Retrofit client = RetrofitClient.getClient("https://api.darksky.net/forecast/"+APIService.WEATHER_API_KEY+"/"+location.getLatitude()+","+location.getLongitude()+"/");
        APIService apiService = client.create(APIService.class);
        Call<WeatherForecast> call = apiService.getWeather("minutely,daily,alerts,flags");
        call.enqueue(this);
    }
    @Override
    public void onResponse(Call<WeatherForecast> call, Response<WeatherForecast> response) {
        mProgress.hide();
        WeatherForecast weatherForecast = response.body();
        if(weatherForecast != null){
            CalendarEventManager.getCalendarEventManager().setWeatherForecast(weatherForecast);
            java.util.Calendar dayAhead = java.util.Calendar.getInstance();
            dayAhead.add(java.util.Calendar.DAY_OF_YEAR ,1);
            for(long now = java.util.Calendar.getInstance().getTimeInMillis(); ; now+=Utils.MILLIS_IN_DAY){
                if(now>dayAhead.getTimeInMillis()){
                    break;
                }
                CalendarEvent event = new CalendarEvent();
                event.setStartDate(now);
                event.setWeatherEvent(true);
                CalendarEventManager.getCalendarEventManager().addToEventMap(event);
            }
            eventsAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onFailure(Call<WeatherForecast> call, Throwable t) {
        mProgress.hide();
        Log.e("MainActivity", "onFailure", t);
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
           Toast.makeText(this,"No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_CALENDAR)
    private void fetchCalendarEvent() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.READ_CALENDAR)) {
            CalendarEventManager.getCalendarEventManager().getRawCalendarEvents(this);
            setEvents();
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_CALENDAR,
                    Manifest.permission.READ_CALENDAR);
        }
    }


    @AfterPermissionGranted(REQUEST_PERMISSION_GET_LOCATION)
    private void fetchLocation() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            boolean isGPSEnabled = false;
            boolean isNetworkEnabled = false;
            Location location = null;
            try {
                LocationManager locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);

                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    Toast.makeText(this, "Sorry cannot find location to fetch weather", Toast.LENGTH_SHORT).show();
                } else {
                    if (isGPSEnabled) {

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        }

                    }
                    if (isNetworkEnabled) {
                        if (location == null) {
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            }
                        }
                    }
                }

                if (location != null) {
                    fetchWeather(location);
                } else {
                    Toast.makeText(this, "Sorry cannot find location to fetch weather", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                   Toast.makeText(this,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.",Toast.LENGTH_SHORT).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void pinChanged(Object tag) {
        java.util.Calendar calendar = (java.util.Calendar)tag;
        calendarViewCustom.setDate(calendar.getTimeInMillis());
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<Event>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("CalendarApp")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<Event> doInBackground(Void... params) {
            List<Event> events = new ArrayList<>();
            try {
                CalendarList calendarList = mService.calendarList().list().execute();
                for (CalendarListEntry entry:calendarList.getItems()) {
                    events.addAll(getDataFromApi(entry.getId()));
                }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
            return events;
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<Event> getDataFromApi(String calendarId) throws IOException {
            Log.d("Varun","TimeZone.getDefault().getID(): "+TimeZone.getDefault().getID());
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = mService.events().list(calendarId)
                    .setMaxResults(100).setTimeMin(new DateTime(EventsAdapter.minDate.getTimeInMillis()))
                    .setTimeMax(new DateTime(EventsAdapter.maxDate.getTimeInMillis()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true).setTimeZone(TimeZone.getDefault().getID())
                    .execute();
            List<Event> items = events.getItems();

            return items;
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<Event> output) {
            mProgress.hide();
            mProgress = null;
            if (output == null || output.size() == 0) {
                Log.d("Varun","No results returned." );
                return;
            }
            Log.d("Varun","adding google events");
            for (Event event : output) {
                CalendarEvent calendarEvent = new CalendarEvent();

                calendarEvent.setTitle(event.getSummary());
                calendarEvent.setDescription(event.getDescription());
                DateTime dateTime = event.getStart().getDateTime();
                if(dateTime == null){
                    dateTime = event.getStart().getDate();
                }
                calendarEvent.setTimezone(dateTime.getTimeZoneShift());
                calendarEvent.setStartDate(dateTime.getValue());
                DateTime endDate = event.getEnd().getDateTime();
                if(endDate == null){
                    endDate = event.getEnd().getDate();

                }
                if(dateTime.isDateOnly() && endDate.isDateOnly() && (endDate.getValue() - dateTime.getValue())== CalendarUtils.DAY_IN_MILLIS){
                    calendarEvent.setSingleDayEvent(true);
                }
                calendarEvent.setEndDate(endDate.getValue());
                CalendarEventManager.getCalendarEventManager().addToEventMap(calendarEvent);
                eventsAdapter.notifyDataSetChanged();
            }

        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                   Toast.makeText(MainActivity.this,"The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
               Toast.makeText(MainActivity.this,"Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
