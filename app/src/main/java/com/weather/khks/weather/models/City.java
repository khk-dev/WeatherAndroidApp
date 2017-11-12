package com.weather.khks.weather.models;

/**
 * Created by KhKS on 10/11/2017.
 */

public class City {
    String cityID;
    String cityName;

    public City(){ }

    public City(String cityID, String cityName) {
        this.cityID = cityID;
        this.cityName = cityName;
    }

    public String getCityID() {
        return cityID;
    }

    public String getCityName() {
        return cityName;
    }
}
