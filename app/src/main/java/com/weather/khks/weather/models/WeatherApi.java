package com.weather.khks.weather.models;

/**
 * Created by msi on 12/11/2017.
 */

public class WeatherApi {
    String weatherApiID;
    String weatherApiName;
    String weatherApiPath;

    public WeatherApi(){ }

    public WeatherApi(String weatherApiID, String weatherApiName, String weatherApiPath) {
        this.weatherApiID = weatherApiID;
        this.weatherApiName = weatherApiName;
        this.weatherApiPath = weatherApiPath;
    }

    public String getWeatherApiID() {
        return weatherApiID;
    }

    public String getWeatherApiName() {
        return weatherApiName;
    }

    public String getWeatherApiPath() {
        return weatherApiPath;
    }
}
