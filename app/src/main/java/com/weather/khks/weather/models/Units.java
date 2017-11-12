package com.weather.khks.weather.models;

import org.json.JSONObject;

/**
 * Created by msi on 12/11/2017.
 */

public class Units implements JSONPopulator {
    private String pressure;
    private String temperature;

    public String getPressure() {
        return pressure;
    }

    public String getTemperature() {
        return temperature;
    }

    @Override
    public void populate(JSONObject data) {
        // pressure element
        pressure = data.optString("pressure");
        // temperature element
        temperature = data.optString("temperature");
    }
}
