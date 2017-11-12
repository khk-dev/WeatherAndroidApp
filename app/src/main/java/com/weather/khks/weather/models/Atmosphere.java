package com.weather.khks.weather.models;

import org.json.JSONObject;

/**
 * Created by msi on 12/11/2017.
 */

public class Atmosphere implements JSONPopulator{
    private Double humidity;
    private Double pressure;

    public Double getHumidity() {
        return humidity;
    }

    public Double getPressure() {
        return pressure;
    }

    @Override
    public void populate(JSONObject data) {
        humidity = data.optDouble("humidity");
        pressure = data.optDouble("pressure");
    }
}
