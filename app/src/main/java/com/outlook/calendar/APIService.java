package com.outlook.calendar;

/**
 * Created by varunarora on 29/08/17.
 */
import retrofit2.Call;
import retrofit2.http.*;

public interface APIService {

    String WEATHER_API_KEY = "268f8637d79a95dc78d404f1c4dc3247";
    @GET(".")
    Call<WeatherForecast> getWeather(@Query("exclude") String excludes);
}
