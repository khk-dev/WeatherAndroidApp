package com.weather.khks.weather.services;

import com.weather.khks.weather.models.Channel;

/**
 * Created by msi on 12/11/2017.
 */

public interface WeatherServiceCallback {
    void serviceSuccess(Channel channel);

    void serviceFailure(Exception exception);
}
