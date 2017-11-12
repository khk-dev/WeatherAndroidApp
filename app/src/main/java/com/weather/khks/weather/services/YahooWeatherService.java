package com.weather.khks.weather.services;

import android.net.Uri;
import android.os.AsyncTask;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.weather.khks.weather.models.Channel;
import com.weather.khks.weather.models.City;
import com.weather.khks.weather.models.WeatherApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by msi on 12/11/2017.
 */

public class YahooWeatherService {
    private static final String YAHOO_WEATHER_API_NAME = "yahoo";
    private WeatherServiceCallback callback;
    private String apiPath;
    private String location;
    private Exception error;

    DatabaseReference databaseWeather;
    //List<WeatherApi> weatherApiList;

    public YahooWeatherService(WeatherServiceCallback callback) {
        this.callback = callback;
        // FirebaseDatabase
        //this.databaseWeather = FirebaseDatabase.getInstance().getReference("weather_api").child("yahoo_weather_api_path");
        this.databaseWeather = FirebaseDatabase.getInstance().getReference("weather_api");
        //weatherApiList = new ArrayList<>();
    }

    public String getLocation() {
        return location;
    }

    public void refreshWeather(String loc) {
        this.location = loc;

        //Query query = databaseWeather.orderByChild("yahoo_weather_api_path").equalTo(checkCityName.toUpperCase());
        databaseWeather.orderByChild("yahoo_weather_api_path").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //WeatherApi weatherApi = dataSnapshot.getValue(WeatherApi.class);
                //String test = "123";

                // clear previous data
                //weatherApiList.clear();

                // retrieve yahoo webapi
                WeatherApi yahooWeatherApi = null;
                for(DataSnapshot weatherApiSnapshot:dataSnapshot.getChildren()){
                    WeatherApi weatherApi = weatherApiSnapshot.getValue(WeatherApi.class);
                    //weatherApiList.add(weatherApi);
                    if (weatherApi.getWeatherApiName().equals(YAHOO_WEATHER_API_NAME)) {
                        yahooWeatherApi = weatherApi;
                    }
                }
                apiPath = yahooWeatherApi.getWeatherApiPath();

                new AsyncTask<String, Void, String>() {
                    @Override
                    protected String doInBackground(String... strings) {

                        String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\") and u='c'", strings[0]);

                        //String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));
                        String endpoint = String.format(strings[1], Uri.encode(YQL));

                        try {
                            URL url = new URL(endpoint);

                            URLConnection connection = url.openConnection();

                            InputStream inputStream = connection.getInputStream();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            StringBuilder result = new StringBuilder();
                            String line;
                            while((line=reader.readLine()) != null) {
                                result.append(line);
                            }

                            return result.toString();

                        } catch (Exception e) {
                            error = e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if(s == null && error != null) {
                            callback.serviceFailure(error);
                            return;
                        }

                        try {
                            JSONObject data = new JSONObject(s);

                            JSONObject queryResults = data.optJSONObject("query");

                            int count = queryResults.optInt("count");

                            // "count" will be 0 if there is no info for searching city(incorrect city name).
                            if(count == 0){
                                callback.serviceFailure(new LocationWeatherException("No weather information found for " + location));
                                return;
                            }

                            Channel channel = new Channel();
                            channel.populate(queryResults.optJSONObject("results").optJSONObject("channel"));

                            callback.serviceSuccess(channel);
                        }
                        catch (JSONException e) {
                            callback.serviceFailure(e);
                        }
                    }
                }.execute(location, apiPath);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.serviceFailure(new LocationWeatherException("Fail to load API Path!"));
            }
        });


    }

    public class LocationWeatherException extends Exception {
        public LocationWeatherException(String detailMessage) {
            super(detailMessage);
        }
    }
}
